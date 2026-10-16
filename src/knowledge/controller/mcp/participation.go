package mcpcontroller

import (
	"github.com/modelcontextprotocol/go-sdk/mcp"
	"github.com/wskr00/dianysma/src/knowledge/service"
)

func registerParticipation(server *mcp.Server, services *service.Services) {
	addTool(server, &mcp.Tool{Name: "get_participation", Description: "Busca uma participação profissional pelo UUID. Use-a para conferir os vínculos com experiência e projeto antes de atualizar ou associar fatos profissionais.", Annotations: &mcp.ToolAnnotations{Title: "Consultar participação", ReadOnlyHint: true, DestructiveHint: new(false), IdempotentHint: true, OpenWorldHint: new(false)}}, services.Participation.Get)
	addTool(server, &mcp.Tool{Name: "list_participations", Description: "Lista as participações da atualização mais recente à mais antiga. A resposta contém a coleção `participations`.", Annotations: &mcp.ToolAnnotations{Title: "Listar participações", ReadOnlyHint: true, DestructiveHint: new(false), IdempotentHint: true, OpenWorldHint: new(false)}}, services.Participation.List)
	addTool(server, &mcp.Tool{Name: "create_participation", Description: "Cria uma participação e retorna seu UUID para associação a fatos profissionais. Informe pelo menos uma experiência ou um projeto já existente; pode informar ambos. A mesma combinação de experiência e projeto só pode existir uma vez.", Annotations: &mcp.ToolAnnotations{Title: "Criar participação", ReadOnlyHint: false, DestructiveHint: new(false), IdempotentHint: false, OpenWorldHint: new(false)}}, services.Participation.Create)
	addTool(server, &mcp.Tool{Name: "update_participation", Description: "Substitui os vínculos e a descrição da participação. Consulte `get_participation` antes: cada vínculo ou descrição opcional omitido/null será removido. Informe pelo menos `experienceId` ou `projectId`; envie todos os vínculos que deseja manter.", Annotations: &mcp.ToolAnnotations{Title: "Atualizar participação", ReadOnlyHint: false, DestructiveHint: new(true), IdempotentHint: true, OpenWorldHint: new(false)}}, services.Participation.Update)
	addTool(server, &mcp.Tool{Name: "delete_participation", Description: "Exclui permanentemente a participação quando não há fatos profissionais associados. Se houver fatos vinculados, a exclusão é recusada; se o UUID não existir, retorna `deleted=false`.", Annotations: &mcp.ToolAnnotations{Title: "Excluir participação", ReadOnlyHint: false, DestructiveHint: new(true), IdempotentHint: true, OpenWorldHint: new(false)}}, services.Participation.Delete)
}
