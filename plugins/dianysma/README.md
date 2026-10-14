# Plugin Dianysma

Pacote Agent Plugins do Dianysma para uso com a conexão MCP já registrada no ChatGPT.

- `plugin.json`: identidade e apresentação do plugin.
- `.app.json`: referência à conexão Dianysma registrada no ChatGPT.
- `skills/dianysma/`: instruções, referências e assets da skill.

O arquivo `.app.json` referencia a conexão existente pelo ID do aplicativo. A URL MCP, a autenticação OAuth com Cognito e os escopos são gerenciados pela configuração dessa conexão, fora deste pacote. O pacote não contém client secret ou token.

Como essa referência aponta para uma conexão registrada na conta, este pacote depende dessa conexão e não é uma configuração MCP portátil para outras contas.
