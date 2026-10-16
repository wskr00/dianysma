package mcpcontroller

import (
	"github.com/modelcontextprotocol/go-sdk/mcp"
	"github.com/wskr00/dianysma/src/knowledge/service"
)

func registerEducation(server *mcp.Server, services *service.Services) {
	addTool(server, &mcp.Tool{Name: "get_education", Description: "Busca uma formação acadêmica ou profissional pelo UUID. Use-a para consultar os dados atuais antes de atualizar.", Annotations: &mcp.ToolAnnotations{Title: "Consultar formação", ReadOnlyHint: true, DestructiveHint: new(false), IdempotentHint: true, OpenWorldHint: new(false)}}, services.Education.Get)
	addTool(server, &mcp.Tool{Name: "list_education", Description: "Lista as formações do registro atualizado mais recentemente ao mais antigo. A resposta contém a coleção `educations`.", Annotations: &mcp.ToolAnnotations{Title: "Listar formações", ReadOnlyHint: true, DestructiveHint: new(false), IdempotentHint: true, OpenWorldHint: new(false)}}, services.Education.List)
	addTool(server, &mcp.Tool{Name: "create_education", Description: "Registra uma formação acadêmica ou profissional e retorna o UUID criado. Escolha `CERTIFICATION` para certificação, `COURSE` para curso ou `DEGREE` para graduação ou pós-graduação. Use datas no formato AAAA-MM-DD.", Annotations: &mcp.ToolAnnotations{Title: "Criar formação", ReadOnlyHint: false, DestructiveHint: new(false), IdempotentHint: false, OpenWorldHint: new(false)}}, services.Education.Create)
	addTool(server, &mcp.Tool{Name: "update_education", Description: "Substitui os dados da formação. Consulte `get_education` antes para preservar valores existentes: instituição, tipo e título são obrigatórios; cada campo opcional omitido ou null será apagado.", Annotations: &mcp.ToolAnnotations{Title: "Atualizar formação", ReadOnlyHint: false, DestructiveHint: new(true), IdempotentHint: true, OpenWorldHint: new(false)}}, services.Education.Update)
	addTool(server, &mcp.Tool{Name: "delete_education", Description: "Exclui permanentemente a formação e remove seu índice de busca semântica, se houver. Se o UUID não existir, retorna `deleted=false`.", Annotations: &mcp.ToolAnnotations{Title: "Excluir formação", ReadOnlyHint: false, DestructiveHint: new(true), IdempotentHint: true, OpenWorldHint: new(false)}}, services.Education.Delete)
}
