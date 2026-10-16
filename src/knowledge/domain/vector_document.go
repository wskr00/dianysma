package domain

import (
	"time"

	"github.com/google/uuid"
	"github.com/pgvector/pgvector-go"
)

type VectorDocument struct {
	ID        uuid.UUID `gorm:"type:uuid;primaryKey"`
	Content   string
	Metadata  map[string]string `gorm:"serializer:json;type:jsonb;not null"`
	Embedding pgvector.Vector   `gorm:"type:vector(1536)"`
}

func (VectorDocument) TableName() string { return "vector_store" }

type KnowledgeChunk struct {
	ID              uuid.UUID  `gorm:"type:uuid;primaryKey"`
	ParticipationID *uuid.UUID `gorm:"type:uuid"`
	ClaimID         *uuid.UUID `gorm:"type:uuid"`
	EducationID     *uuid.UUID `gorm:"type:uuid"`
	Hash            string     `gorm:"size:128;not null"`
	Model           string     `gorm:"size:100;not null"`
	IndexedAt       time.Time  `gorm:"not null;autoCreateTime;autoUpdateTime"`
}
