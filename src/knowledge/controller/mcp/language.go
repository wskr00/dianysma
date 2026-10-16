package mcpcontroller

import (
	"github.com/modelcontextprotocol/go-sdk/mcp"
	"github.com/wskr00/dianysma/src/knowledge/service"
)

func registerLanguage(server *mcp.Server, services *service.Services) {
	addTool(server, &mcp.Tool{Name: "get_language", Description: "Consulta um idioma do perfil pelo UUID.", Annotations: &mcp.ToolAnnotations{Title: "Consultar idioma", ReadOnlyHint: true, DestructiveHint: new(false), IdempotentHint: true, OpenWorldHint: new(false)}}, services.Language.Get)
	addTool(server, &mcp.Tool{Name: "list_languages", Description: "Lista em ordem alfabética os idiomas associados ao perfil pessoal. A resposta contém a coleção `languages`. Se ela já contiver o idioma, use `update_language` em vez de criar outro registro.", Annotations: &mcp.ToolAnnotations{Title: "Listar idiomas", ReadOnlyHint: true, DestructiveHint: new(false), IdempotentHint: true, OpenWorldHint: new(false)}}, services.Language.List)
	addTool(server, &mcp.Tool{Name: "create_language", Description: "Registra um idioma no perfil pessoal e retorna seu UUID. Crie o perfil com `create_profile` antes. Consulte `list_languages` para evitar duplicar um idioma já registrado.", Annotations: &mcp.ToolAnnotations{Title: "Criar idioma", ReadOnlyHint: false, DestructiveHint: new(false), IdempotentHint: false, OpenWorldHint: new(false)}}, services.Language.Create)
	addTool(server, &mcp.Tool{Name: "update_language", Description: "Substitui o nome e a proficiência do idioma identificado. Consulte `get_language` ou `list_languages` para conferir o registro antes de alterá-lo.", Annotations: &mcp.ToolAnnotations{Title: "Atualizar idioma", ReadOnlyHint: false, DestructiveHint: new(true), IdempotentHint: true, OpenWorldHint: new(false)}}, services.Language.Update)
	addTool(server, &mcp.Tool{Name: "delete_language", Description: "Exclui um idioma do perfil. Se o ID não existir, retorna deleted=false.", Annotations: &mcp.ToolAnnotations{Title: "Excluir idioma", ReadOnlyHint: false, DestructiveHint: new(true), IdempotentHint: true, OpenWorldHint: new(false)}}, services.Language.Delete)
}
