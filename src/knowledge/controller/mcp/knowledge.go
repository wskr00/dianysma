package mcpcontroller

import (
	"github.com/modelcontextprotocol/go-sdk/mcp"
	"github.com/wskr00/dianysma/src/knowledge/service"
)

func registerKnowledge(server *mcp.Server, services *service.Services) {
	addTool(server, &mcp.Tool{Name: "search_knowledge", Description: "Recupera por significado fatos profissionais, participações e formações indexados. A resposta contém a coleção `results`; cada resultado traz `sourceType`, `sourceId`, um trecho e uma pontuação de similaridade. A pontuação ordena relevância, não é probabilidade nem confirmação factual. Para obter os dados completos, use `get_claim`, `get_participation` ou `get_education` conforme `sourceType` (`claim`, `participation` ou `education`).", Annotations: &mcp.ToolAnnotations{Title: "Pesquisar conhecimento profissional", ReadOnlyHint: true, DestructiveHint: new(false), IdempotentHint: true, OpenWorldHint: new(false)}}, services.Knowledge.Search)
}
