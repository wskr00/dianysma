# Migração do backend para Go

## Escopo e organização

Reimplementar as 41 ferramentas MCP e as regras atuais de Career Knowledge em Go, mantendo a implementação Java durante a revisão. Não migrar dados nem executar mudanças no Neon ou no AgentCore nesta etapa.

```text
cmd/dianysma/                         inicialização e encerramento
internal/core/config/                ambiente, .env e AWS Secrets Manager
internal/core/database/              conexão, pool e migrações Goose
internal/core/audit/mcp/             início/fim das chamadas MCP
internal/knowledge/domain/           entidades e enumerações
internal/knowledge/controller/dto/   command e result, arquivos por entidade
internal/knowledge/controller/mcp/   registro e adaptação das ferramentas
internal/knowledge/repository/       GORM, consultas e transações
internal/knowledge/service/          casos de uso e regras
internal/knowledge/service/vector/   documentos, embeddings e sincronização
```

Usar pacotes por responsabilidade; não criar uma interface para cada struct, nem reproduzir proxies e anotações do Spring. Interfaces pequenas serão definidas onde houver uma integração substituível, como embeddings. `context.Context` acompanha banco, MCP e chamadas externas.

## Bibliotecas e execução

- Go estável mais recente; SDK oficial `modelcontextprotocol/go-sdk` com Streamable HTTP stateless e schemas inferidos de DTOs tipados.
- GORM com driver PostgreSQL/pgx, `pgvector-go`, UUIDv7 e Goose com migrações embarcadas. Schema novo para a aplicação Go; sem conversão do histórico Flyway.
- SDK Google Gen AI para Gemini Embedding 2 e SDK AWS v2 para Secrets Manager. Validação de DTOs com `go-playground/validator`.
- `net/http`, `log/slog`, limites de requisição e timeouts; encerramento por sinal e cancelamento de contexto. O próprio servidor HTTP atende requisições concorrentemente. Escritas e indexação permanecem síncronas e atômicas no PostgreSQL.
- Binário Linux ARM64 sem JVM/GraalVM. Dockerfile Go independente enquanto Java e a pipeline atual são preservados.

## Regras que precisam permanecer

- Perfil único; campos opcionais ausentes viram `null` nas saídas. Atualizações substituem os opcionais, exceto os vínculos de Claim, que mantêm a semântica específica de omissão, lista vazia e `clearParticipation`.
- Competência canônica por nome sem distinção de caixa; criação concorrente reutiliza a existente e preserva aliases. Listas vazias são `[]`.
- FKs, combinação única de participação e exclusões protegidas por vínculos; UUIDv7 e auditoria automática pelo ORM.
- Claim, Participation com descrição e Education geram documentos vetoriais. Hash de conteúdo/configuração evita chamadas redundantes. Alterar organização, experiência ou projeto atualiza as participações afetadas.
- Alteração canônica, vetor e KnowledgeChunk compartilham uma transação; falha de embedding desfaz a alteração. Busca por cosseno usa HNSW, retorna tipo/ID canônico e trecho Unicode de até 500 caracteres.
- Nomes de tools em inglês; descrições e erros em português. Logs de início/fim identificam a ferramenta, status HTTP e duração, sem payloads ou identidade.
- Autenticação continua na infraestrutura Cognito/AgentCore. O processo Go não vira Authorization Server nem duplica a autenticação existente.

## Validação

Compilar e executar `go vet`, testes de casos de uso, integração PostgreSQL/pgvector em container descartável e chamadas MCP reais pelo SDK. Substituir somente o provedor de embeddings por um servidor sintético nos testes; validar também o formato HTTP usado pelo SDK Google. Cobrir constraints, rollback, schemas nullable, todas as ferramentas, mudanças de contexto e invalidação do índice. Gerar também o binário ARM64 e verificar o Dockerfile Go. Nenhum teste usa banco ou credenciais de produção.
