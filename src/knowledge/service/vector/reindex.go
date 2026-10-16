package vector

import (
	"context"
	"log/slog"

	"github.com/google/uuid"
	"github.com/wskr00/dianysma/src/knowledge/repository"
)

type Reindexer struct {
	repo    *repository.Store
	indexer *Indexer
	logger  *slog.Logger
}

func NewReindexer(repo *repository.Store, indexer *Indexer, logger *slog.Logger) *Reindexer {
	return &Reindexer{repo: repo, indexer: indexer, logger: logger}
}

func (r *Reindexer) Run(ctx context.Context, force bool) error {
	changed, err := r.repo.HasDifferentModel(ctx, r.indexer.vectors.ModelSignature())
	if err != nil {
		return err
	}
	if !force && !changed {
		return nil
	}
	r.logger.InfoContext(ctx, "Reindexando conhecimento", "model", r.indexer.vectors.ModelSignature())
	for _, source := range []string{"claim", "participation", "education"} {
		cursor := uuid.Nil
		for {
			ids, err := r.repo.SourceIDs(ctx, source, cursor, 100)
			if err != nil {
				return err
			}
			if len(ids) == 0 {
				break
			}
			for _, id := range ids {
				err := r.repo.Transaction(ctx, func(tx *repository.Store) error {
					switch source {
					case "claim":
						if _, err := tx.Claims.Lock(ctx, id); err != nil {
							return ignoreMissing(err)
						}
						value, err := tx.Claim(ctx, id)
						if err != nil {
							return err
						}
						return r.indexer.IndexClaim(ctx, tx, value)
					case "participation":
						if _, err := tx.Participations.Lock(ctx, id); err != nil {
							return ignoreMissing(err)
						}
						return r.indexer.IndexParticipation(ctx, tx, id)
					case "education":
						value, err := tx.Educations.Lock(ctx, id)
						if err != nil {
							return ignoreMissing(err)
						}
						return r.indexer.IndexEducation(ctx, tx, value)
					}
					return nil
				})
				if err != nil {
					return err
				}
			}
			cursor = ids[len(ids)-1]
		}
	}
	r.logger.InfoContext(ctx, "Reindexação concluída")
	return nil
}

func ignoreMissing(err error) error {
	if repository.IsNotFound(err) {
		return nil
	}
	return err
}
