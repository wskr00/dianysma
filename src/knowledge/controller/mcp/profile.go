package mcpcontroller

import (
	"github.com/modelcontextprotocol/go-sdk/mcp"
	"github.com/wskr00/dianysma/src/knowledge/service"
)

func registerProfile(server *mcp.Server, services *service.Services) {
	addTool(server, &mcp.Tool{Name: "get_profile", Description: "Consulta o único perfil pessoal desta instalação. Se ainda não existir, use `create_profile` antes de cadastrar idiomas.", Annotations: &mcp.ToolAnnotations{Title: "Consultar perfil", ReadOnlyHint: true, DestructiveHint: new(false), IdempotentHint: true, OpenWorldHint: new(false)}}, services.Profile.Get)
	addTool(server, &mcp.Tool{Name: "create_profile", Description: "Cria o único perfil pessoal desta instalação e retorna seu UUID. Todos os campos são opcionais. Use `get_profile` primeiro para verificar se já existe; para alterá-lo, use `update_profile`.", Annotations: &mcp.ToolAnnotations{Title: "Criar perfil", ReadOnlyHint: false, DestructiveHint: new(false), IdempotentHint: false, OpenWorldHint: new(false)}}, services.Profile.Create)
	addTool(server, &mcp.Tool{Name: "update_profile", Description: "Substitui todos os dados do perfil; não é uma atualização parcial. Consulte `get_profile` e reenvie os valores que deseja manter. Qualquer campo omitido ou null será apagado. Por exemplo, para alterar só a cidade, envie também os demais valores atuais.", Annotations: &mcp.ToolAnnotations{Title: "Atualizar perfil", ReadOnlyHint: false, DestructiveHint: new(true), IdempotentHint: true, OpenWorldHint: new(false)}}, services.Profile.Update)
}
