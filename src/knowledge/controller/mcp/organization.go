package mcpcontroller

import (
	"github.com/modelcontextprotocol/go-sdk/mcp"
	"github.com/wskr00/dianysma/src/knowledge/service"
)

func registerOrganization(server *mcp.Server, services *service.Services) {
	addTool(server, &mcp.Tool{Name: "get_organization", Description: "Busca uma organização pelo UUID. Use o registro retornado para confirmar o ID antes de associá-lo a uma experiência ou projeto.", Annotations: &mcp.ToolAnnotations{Title: "Consultar organização", ReadOnlyHint: true, DestructiveHint: new(false), IdempotentHint: true, OpenWorldHint: new(false)}}, services.Organization.Get)
	addTool(server, &mcp.Tool{Name: "search_organizations", Description: "Pesquisa organizações por trecho do nome, sem diferenciar maiúsculas de minúsculas. A resposta contém a coleção `organizations`. Use o UUID retornado ao criar uma experiência ou projeto; reutilize uma organização existente em vez de duplicá-la.", Annotations: &mcp.ToolAnnotations{Title: "Pesquisar organizações", ReadOnlyHint: true, DestructiveHint: new(false), IdempotentHint: true, OpenWorldHint: new(false)}}, services.Organization.Search)
	addTool(server, &mcp.Tool{Name: "create_organization", Description: "Cria uma organização pelo nome e retorna seu UUID. Antes, use `search_organizations` para reutilizar uma organização já cadastrada; o servidor não combina registros duplicados automaticamente.", Annotations: &mcp.ToolAnnotations{Title: "Criar organização", ReadOnlyHint: false, DestructiveHint: new(false), IdempotentHint: false, OpenWorldHint: new(false)}}, services.Organization.Create)
	addTool(server, &mcp.Tool{Name: "update_organization", Description: "Substitui o nome da organização identificada sem alterar seus vínculos. Consulte `get_organization` antes para confirmar o registro e evitar sobrescrever o nome errado.", Annotations: &mcp.ToolAnnotations{Title: "Atualizar organização", ReadOnlyHint: false, DestructiveHint: new(true), IdempotentHint: true, OpenWorldHint: new(false)}}, services.Organization.Update)
	addTool(server, &mcp.Tool{Name: "delete_organization", Description: "Exclui permanentemente uma organização somente quando não há experiências nem projetos associados. Se houver vínculos, a exclusão é recusada; se o UUID não existir, retorna `deleted=false`.", Annotations: &mcp.ToolAnnotations{Title: "Excluir organização", ReadOnlyHint: false, DestructiveHint: new(true), IdempotentHint: true, OpenWorldHint: new(false)}}, services.Organization.Delete)
}
