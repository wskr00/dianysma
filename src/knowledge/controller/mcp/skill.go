package mcpcontroller

import (
	"github.com/modelcontextprotocol/go-sdk/mcp"
	"github.com/wskr00/dianysma/src/knowledge/service"
)

func registerSkill(server *mcp.Server, services *service.Services) {
	addTool(server, &mcp.Tool{Name: "search_skills", Description: "Pesquisa por trecho do nome canônico ou de um nome alternativo, sem diferenciar maiúsculas de minúsculas. A resposta contém a coleção `skills`. Use o UUID retornado ao criar fatos profissionais; reutilize a competência existente para manter o catálogo canônico.", Annotations: &mcp.ToolAnnotations{Title: "Pesquisar competências", ReadOnlyHint: true, DestructiveHint: new(false), IdempotentHint: true, OpenWorldHint: new(false)}}, services.Skill.Search)
	addTool(server, &mcp.Tool{Name: "create_skill", Description: "Cria uma competência canônica para associação a fatos profissionais e retorna seu UUID. Se já existir uma com o mesmo nome, ignorando maiúsculas e minúsculas, retorna a existente sem alterar nome ou aliases. Informe aliases apenas para uma competência nova.", Annotations: &mcp.ToolAnnotations{Title: "Criar competência", ReadOnlyHint: false, DestructiveHint: new(false), IdempotentHint: true, OpenWorldHint: new(false)}}, services.Skill.Create)
}
