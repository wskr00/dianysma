package mcpcontroller

import (
	"github.com/modelcontextprotocol/go-sdk/mcp"
	"github.com/wskr00/dianysma/src/knowledge/service"
)

func registerExperience(server *mcp.Server, services *service.Services) {
	addTool(server, &mcp.Tool{Name: "get_experience", Description: "Busca uma experiência profissional pelo UUID. Consulte-a antes de atualizar se precisar preservar os valores atuais.", Annotations: &mcp.ToolAnnotations{Title: "Consultar experiência", ReadOnlyHint: true, DestructiveHint: new(false), IdempotentHint: true, OpenWorldHint: new(false)}}, services.Experience.Get)
	addTool(server, &mcp.Tool{Name: "list_experiences", Description: "Lista as experiências profissionais da atualização mais recente à mais antiga. A resposta contém a coleção `experiences`.", Annotations: &mcp.ToolAnnotations{Title: "Listar experiências", ReadOnlyHint: true, DestructiveHint: new(false), IdempotentHint: true, OpenWorldHint: new(false)}}, services.Experience.List)
	addTool(server, &mcp.Tool{Name: "create_experience", Description: "Registra uma experiência profissional e retorna seu UUID. `employerId` deve identificar a organização empregadora; `clientId` é opcional e identifica a organização cliente, que pode ser a própria empregadora. O tipo de vínculo é texto livre, por exemplo CLT, PJ, estágio ou freelancer. Datas e descrição são opcionais.", Annotations: &mcp.ToolAnnotations{Title: "Criar experiência", ReadOnlyHint: false, DestructiveHint: new(false), IdempotentHint: false, OpenWorldHint: new(false)}}, services.Experience.Create)
	addTool(server, &mcp.Tool{Name: "update_experience", Description: "Substitui os dados da experiência. Consulte `get_experience` antes para preservar valores atuais. Empregadora, cargo e tipo de vínculo são obrigatórios; campos opcionais omitidos ou null serão apagados.", Annotations: &mcp.ToolAnnotations{Title: "Atualizar experiência", ReadOnlyHint: false, DestructiveHint: new(true), IdempotentHint: true, OpenWorldHint: new(false)}}, services.Experience.Update)
	addTool(server, &mcp.Tool{Name: "delete_experience", Description: "Exclui permanentemente uma experiência quando não há participações associadas. Se houver participações, a exclusão é recusada; se o UUID não existir, retorna `deleted=false`.", Annotations: &mcp.ToolAnnotations{Title: "Excluir experiência", ReadOnlyHint: false, DestructiveHint: new(true), IdempotentHint: true, OpenWorldHint: new(false)}}, services.Experience.Delete)
}
