package main

import (
	"context"
	"errors"
	"fmt"
	"log/slog"
	"net"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/wskr00/dianysma/src/core/config"
	"github.com/wskr00/dianysma/src/core/database"
	mcpcontroller "github.com/wskr00/dianysma/src/knowledge/controller/mcp"
	"github.com/wskr00/dianysma/src/knowledge/repository"
	"github.com/wskr00/dianysma/src/knowledge/service"
	"github.com/wskr00/dianysma/src/knowledge/service/vector"
)

var version = "dev"

const (
	requestTimeout   = time.Minute
	shutdownTimeout  = 15 * time.Second
	embeddingTimeout = 45 * time.Second
)

func main() {
	logger := slog.New(slog.NewJSONHandler(os.Stdout, nil))
	slog.SetDefault(logger)
	ctx, stop := signal.NotifyContext(context.Background(), os.Interrupt, syscall.SIGTERM)
	defer stop()
	if err := run(ctx, logger); err != nil {
		logger.Error("Não foi possível executar o Dianysma", "error", err)
		os.Exit(1)
	}
}

func run(ctx context.Context, logger *slog.Logger) (runErr error) {
	mode := "serve"
	if len(os.Args) > 1 {
		mode = os.Args[1]
	}
	if mode != "serve" && mode != "migrate" && mode != "reindex" {
		return errors.New("use dianysma [serve|migrate|reindex]")
	}
	c, err := config.Load(ctx)
	if err != nil {
		return err
	}
	migrationCtx, cancel := context.WithTimeout(ctx, 2*time.Minute)
	err = database.Migrate(migrationCtx, c)
	cancel()
	if err != nil {
		return fmt.Errorf("aplicar migrações: %w", err)
	}
	if mode == "migrate" {
		return nil
	}
	db, err := database.Open(ctx, c)
	if err != nil {
		return err
	}
	pool, err := db.DB()
	if err != nil {
		return err
	}
	defer func() {
		if err := pool.Close(); err != nil {
			runErr = errors.Join(runErr, fmt.Errorf("fechar pool PostgreSQL: %w", err))
		}
	}()
	if err := database.CheckDimensions(ctx, db, c.EmbeddingDimensions); err != nil {
		return err
	}
	embedder, err := vector.NewGoogleEmbedder(ctx, c.GeminiAPIKey, c.EmbeddingModel, c.EmbeddingDimensions, embeddingTimeout)
	if err != nil {
		return err
	}
	repo := repository.New(db)
	vectors := vector.NewStore(embedder, c.EmbeddingModel, c.EmbeddingDimensions)
	indexer := vector.NewIndexer(vectors)
	if c.AutoReindex || mode == "reindex" {
		if err := vector.NewReindexer(repo, indexer, logger).Run(ctx, mode == "reindex"); err != nil {
			return err
		}
	} else {
		logger.InfoContext(ctx, "Reindexação automática desativada")
	}
	if mode == "reindex" {
		return nil
	}
	services, err := service.New(repo, indexer, vectors)
	if err != nil {
		return err
	}
	mcpServer := mcpcontroller.NewServer(services, logger, version, requestTimeout)
	protection := http.NewCrossOriginProtection()
	mux := http.NewServeMux()
	mux.Handle(c.Endpoint, protection.Handler(mcpcontroller.NewHandler(mcpServer, logger, c.DisableLocalhostProtection)))
	serverCtx, cancel := context.WithCancel(context.Background())
	defer cancel()
	server := &http.Server{Addr: fmt.Sprintf(":%d", c.Port), Handler: mux,
		ReadHeaderTimeout: 10 * time.Second, ReadTimeout: 15 * time.Second, WriteTimeout: requestTimeout + 5*time.Second,
		IdleTimeout: time.Minute, MaxHeaderBytes: 16 << 10,
		BaseContext: func(net.Listener) context.Context { return serverCtx },
		ErrorLog:    slog.NewLogLogger(logger.Handler(), slog.LevelError),
	}
	listener, err := net.Listen("tcp", server.Addr)
	if err != nil {
		return err
	}
	errorsCh := make(chan error, 1)
	go func() { errorsCh <- server.Serve(listener) }()
	logger.Info("Dianysma iniciado", "version", version, "port", c.Port, "mcp_endpoint", c.Endpoint)
	select {
	case err := <-errorsCh:
		if errors.Is(err, http.ErrServerClosed) {
			return nil
		}
		return err
	case <-ctx.Done():
		shutdownCtx, stop := context.WithTimeout(context.Background(), shutdownTimeout)
		defer stop()
		if err := server.Shutdown(shutdownCtx); err != nil {
			cancel()
			shutdownErr := fmt.Errorf("encerrar servidor: %w", err)
			if closeErr := server.Close(); closeErr != nil {
				shutdownErr = errors.Join(shutdownErr, fmt.Errorf("fechar servidor: %w", closeErr))
			}
			return shutdownErr
		}
		return nil
	}
}
