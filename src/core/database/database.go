package database

import (
	"context"
	"database/sql"
	"embed"
	"errors"
	"fmt"
	"io/fs"
	"time"

	"github.com/pressly/goose/v3"
	"github.com/pressly/goose/v3/lock"
	"gorm.io/driver/postgres"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"
	"gorm.io/gorm/schema"

	"github.com/wskr00/dianysma/src/core/config"
)

//go:embed migrations/*.sql
var migrations embed.FS

const (
	maxIdleConnections  = 0
	connectionMaxAge    = 30 * time.Minute
	connectionIdleLimit = 5 * time.Minute
)

func Open(ctx context.Context, c config.Config) (*gorm.DB, error) {
	db, err := gorm.Open(postgres.New(postgres.Config{DSN: c.DSN(false), PreferSimpleProtocol: true}), &gorm.Config{
		NamingStrategy:       schema.NamingStrategy{SingularTable: true},
		Logger:               logger.Default.LogMode(logger.Silent),
		DisableAutomaticPing: true,
	})
	if err != nil {
		return nil, fmt.Errorf("abrir PostgreSQL: %w", err)
	}
	pool, err := db.DB()
	if err != nil {
		return nil, err
	}
	pool.SetMaxOpenConns(c.DatabasePoolMax)
	pool.SetMaxIdleConns(maxIdleConnections)
	pool.SetConnMaxLifetime(connectionMaxAge)
	pool.SetConnMaxIdleTime(connectionIdleLimit)
	pingCtx, cancel := context.WithTimeout(ctx, 10*time.Second)
	defer cancel()
	if err := pool.PingContext(pingCtx); err != nil {
		pingErr := fmt.Errorf("conectar ao PostgreSQL: %w", err)
		if closeErr := pool.Close(); closeErr != nil {
			pingErr = errors.Join(pingErr, fmt.Errorf("fechar pool PostgreSQL: %w", closeErr))
		}
		return nil, pingErr
	}
	return db, nil
}

// Migrate uses a dedicated direct connection for Goose's session advisory lock.
// DATABASE_MIGRATION_HOST should be the direct Neon host when the application uses its pooler.
func Migrate(ctx context.Context, c config.Config) (migrationErr error) {
	db, err := sql.Open("pgx", c.DSN(true))
	if err != nil {
		return err
	}
	defer func() {
		if err := db.Close(); err != nil {
			migrationErr = errors.Join(migrationErr, fmt.Errorf("fechar conexão de migração: %w", err))
		}
	}()
	db.SetMaxOpenConns(1)
	return ApplyMigrations(ctx, db)
}

func ApplyMigrations(ctx context.Context, db *sql.DB) error {
	files, err := fs.Sub(migrations, "migrations")
	if err != nil {
		return err
	}
	locker, err := lock.NewPostgresSessionLocker()
	if err != nil {
		return err
	}
	provider, err := goose.NewProvider(goose.DialectPostgres, db, files, goose.WithSessionLocker(locker))
	if err != nil {
		return err
	}
	_, err = provider.Up(ctx)
	return err
}

func CheckDimensions(ctx context.Context, db *gorm.DB, dimensions int) error {
	var actual string
	err := db.WithContext(ctx).Raw(`SELECT format_type(atttypid, atttypmod)
		FROM pg_attribute WHERE attrelid = 'vector_store'::regclass AND attname = 'embedding'`).Scan(&actual).Error
	if err != nil {
		return err
	}
	if expected := fmt.Sprintf("vector(%d)", dimensions); actual != expected {
		return fmt.Errorf("a coluna embedding usa %s; a configuração requer %s; ajuste o schema e reindexe antes de iniciar", actual, expected)
	}
	return nil
}
