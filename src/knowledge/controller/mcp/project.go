package mcpcontroller

import (
	"github.com/modelcontextprotocol/go-sdk/mcp"
	"github.com/wskr00/dianysma/src/knowledge/service"
)

func registerProject(server *mcp.Server, services *service.Services) {
	addTool(server, &mcp.Tool{Name: "get_project", Description: "Busca um projeto pelo UUID. Use-o para consultar os dados atuais antes de atualizar ou associar o projeto a uma participação.", Annotations: &mcp.ToolAnnotations{Title: "Consultar projeto", ReadOnlyHint: true, DestructiveHint: new(false), IdempotentHint: true, OpenWorldHint: new(false)}}, services.Project.Get)
	addTool(server, &mcp.Tool{Name: "list_projects", Description: "Lista os projetos da atualização mais recente à mais antiga. A resposta contém a coleção `projects`.", Annotations: &mcp.ToolAnnotations{Title: "Listar projetos", ReadOnlyHint: true, DestructiveHint: new(false), IdempotentHint: true, OpenWorldHint: new(false)}}, services.Project.List)
	addTool(server, &mcp.Tool{Name: "create_project", Description: "Cria um projeto e retorna seu UUID para uso em uma participação. A organização é opcional; quando informada, deve existir e ser selecionada por seu UUID.", Annotations: &mcp.ToolAnnotations{Title: "Criar projeto", ReadOnlyHint: false, DestructiveHint: new(false), IdempotentHint: false, OpenWorldHint: new(false)}}, services.Project.Create)
	addTool(server, &mcp.Tool{Name: "update_project", Description: "Substitui os dados do projeto. Consulte `get_project` antes para preservar valores atuais: o nome é obrigatório; organização e descrição omitidas/null serão removidas.", Annotations: &mcp.ToolAnnotations{Title: "Atualizar projeto", ReadOnlyHint: false, DestructiveHint: new(true), IdempotentHint: true, OpenWorldHint: new(false)}}, services.Project.Update)
	addTool(server, &mcp.Tool{Name: "delete_project", Description: "Exclui permanentemente o projeto somente quando não há participações associadas. Se houver vínculos, a exclusão é recusada; se o UUID não existir, retorna `deleted=false`.", Annotations: &mcp.ToolAnnotations{Title: "Excluir projeto", ReadOnlyHint: false, DestructiveHint: new(true), IdempotentHint: true, OpenWorldHint: new(false)}}, services.Project.Delete)
}
