package vector

import (
	"context"
	"errors"
	"math"
	"net/http"
	"time"

	"google.golang.org/genai"
)

// Embedder is the only provider-specific boundary needed by indexing and search.
type Embedder interface {
	Embed(context.Context, string) ([]float32, error)
}

type GoogleEmbedder struct {
	client     *genai.Client
	model      string
	dimensions int32
}

func NewGoogleEmbedder(ctx context.Context, key, model string, dimensions int, timeout time.Duration) (*GoogleEmbedder, error) {
	if key == "" {
		return nil, errors.New("configure GEMINI_API_KEY")
	}
	if dimensions < 1 || int64(dimensions) > math.MaxInt32 {
		return nil, errors.New("GEMINI_EMBEDDING_DIMENSIONS deve caber em um inteiro positivo de 32 bits")
	}
	client, err := genai.NewClient(ctx, &genai.ClientConfig{
		APIKey: key, Backend: genai.BackendGeminiAPI, HTTPClient: &http.Client{Timeout: timeout},
	})
	if err != nil {
		return nil, err
	}
	return NewGoogleEmbedderWithClient(client, model, int32(dimensions)), nil
}

func NewGoogleEmbedderWithClient(client *genai.Client, model string, dimensions int32) *GoogleEmbedder {
	return &GoogleEmbedder{client: client, model: model, dimensions: dimensions}
}

func (g *GoogleEmbedder) Embed(ctx context.Context, text string) ([]float32, error) {
	response, err := g.client.Models.EmbedContent(ctx, g.model, genai.Text(text), &genai.EmbedContentConfig{
		OutputDimensionality: &g.dimensions,
	})
	if err != nil {
		return nil, err
	}
	if len(response.Embeddings) != 1 || response.Embeddings[0] == nil {
		return nil, errors.New("o provedor não retornou exatamente um embedding")
	}
	return response.Embeddings[0].Values, nil
}
