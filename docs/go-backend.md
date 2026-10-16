# Backend Go

A implementação em `cmd/` e `internal/` mantém os casos de uso e as 41 ferramentas do backend Java. Java, Flyway e a pipeline existente permanecem disponíveis durante a revisão; o container Go tem um Dockerfile independente. Não foi realizada migração de dados, alteração de runtime nem acesso a bancos remotos.

## Stack e organização

Go 1.27.2, SDK MCP oficial 1.8, GORM com PostgreSQL/pgx, pgvector-go, Goose, Google Gen AI e AWS SDK v2. As versões exatas ficam em `go.mod` e `go.sum`; `VERSION` identifica a versão do binário na imagem Go. Go publica versões estáveis, sem uma linha LTS separada.

```text
cmd/dianysma/                    composição da aplicação e ciclo de vida
internal/core/
  audit/                        Auditable GORM e logs de chamadas em mcp/
  config/                       ambiente, .env e Secrets Manager
  database/migrations/          migrações Goose embarcadas
  validation/                   validação estrutural em português
internal/knowledge/
  domain/                       entidades, UUIDv7 e auditoria GORM
  controller/dto/command/        entradas tipadas, por entidade
  controller/dto/result/         saídas tipadas e campos nullable
  controller/mcp/               descrições, schemas e adaptação MCP
  repository/                   consultas e unidade de transação
  service/                      regras e casos de uso
    exception/                  erros que podem ser apresentados ao cliente
    vector/                     SDK Google, projeção, hash e reindexação
```

O SDK cuida de negociação do protocolo, serialização, schemas e erros MCP. O transporte é Streamable HTTP stateless, com respostas JSON; o SDK suporta `2026-07-28` e mantém negociação com `2025-11-25`. DTOs de atualização reutilizam os campos de criação quando as regras são iguais. Claim mantém DTO próprio para preservar sua semântica de edição.

O serviço HTTP usa a concorrência do `net/http` e propagação de contexto. Não há goroutines de indexação desacopladas: erros de embedding ou persistência desfazem a escrita canônica, a associação de competências, o vetor e o chunk na mesma transação. Alterações relacionadas usam bloqueios de linha ao atualizar suas projeções. A busca consulta as FKs do chunk para recuperar a identidade canônica.

## Execução local

Pré-requisitos: Go 1.27.2 ou posterior e Docker para PostgreSQL.

```sh
# Acrescente ao .env existente os valores locais de .env.go.example.
docker compose up -d postgres
go run ./cmd/dianysma
```

`POST http://localhost:8000/mcp` atende MCP. O AgentCore usa o endpoint MCP para verificar o runtime; não há endpoint HTTP `/ping` nem API REST de negócio.

Na inicialização, Goose aplica as migrações pendentes. O comando abaixo executa apenas essa etapa, sem exigir chave Gemini:

```sh
go run ./cmd/dianysma migrate
```

O schema Go não adota o histórico Flyway. Use um banco local separado para comparar as implementações até definirmos a transição dos dados. A V1 Go preserva as entidades e regras atuais, excluindo a tabela obsoleta de publicações Modulith.

## Configuração e infraestrutura

As variáveis são exemplificadas em `.env.go.example`. O processo usa ambiente e `.env`; valores já definidos têm precedência sobre o segredo. Não são impressas credenciais nem valores que falharam no parser.

- `DATABASE_HOST`, `DATABASE_PORT`, `DATABASE_NAME`, `DATABASE_USER`, `DATABASE_PASSWORD`: mesmos conceitos do Compose e do Java. A aplicação monta a conexão com escaping de credenciais.
- `DATABASE_SSL_MODE`: padrão `verify-full` para conexões remotas; em desenvolvimento local, use `disable`. O container inclui as autoridades certificadoras do sistema.
- `DATABASE_POOL_MAX_SIZE=4`: máximo de conexões abertas. O máximo de conexões ociosas fica fixo em zero para reduzir conexões retidas no Neon; o driver usa protocolo simples para compatibilidade com o pooler PostgreSQL.
- `DATABASE_MIGRATION_HOST`: necessário no segredo se `DATABASE_HOST` usa o pooler Neon. Goose usa advisory lock de sessão, então as migrações usam o host direto.
- `AWS_SECRET_ID`: segredo JSON no Secrets Manager, acessado pela role do runtime e pelo SDK AWS. Substitui o `SPRING_CONFIG_IMPORT` da aplicação Java. A região vem do ambiente do runtime; não são necessárias chaves AWS estáticas no container.
- `GEMINI_API_KEY`, `GEMINI_EMBEDDING_MODEL=gemini-embedding-2`, `GEMINI_EMBEDDING_DIMENSIONS=1536`: embeddings. A inicialização verifica se as dimensões coincidem com o schema.

Os timeouts, o pool ocioso e a execução de migrações usam padrões internos; não precisam de variáveis próprias. Para forçar uma reindexação completa, use `go run ./cmd/dianysma reindex`.

O servidor não habilita CORS nem aceita origens de navegador diretamente. A proteção contra requisições cross-origin permanece ativa; no MCP Inspector, use a conexão via proxy. Clientes MCP de servidor não enviam uma origem de navegador e não precisam de allowlist.

A autenticação continua no Cognito e no authorizer do AgentCore. Não há JWT, usuários ou Authorization Server no processo Go. O container precisa continuar acessível somente pela infraestrutura autorizada do runtime.

## Vetores e reindexação

`vector_store.id` e `knowledge_chunk.id` usam o UUIDv7 da fonte. A relação entre chunk e vetor mantém `ON DELETE CASCADE`. Claims, participações com descrição e formações são indexadas; as demais entidades são contexto ou catálogo.

SHA-256 inclui texto, modelo e dimensões. Quando esse conteúdo não muda, nenhuma chamada de embedding é feita. O campo `model` do chunk guarda a assinatura `modelo:dimensões`. A mudança de assinatura inicia a reindexação antes de abrir o servidor. O índice é HNSW por cosseno; os trechos são limitados a 500 caracteres Unicode.

Para reconstruir também índices ausentes, execute:

```sh
go run ./cmd/dianysma reindex
```

O processo lê IDs em páginas de 100 e confirma uma fonte por transação, permitindo retomar após falhas. O schema inicial usa 1536 dimensões. Alterar esse tamanho exige uma migração do schema e reindexação; a aplicação recusa iniciar com tamanho incompatível.

## Verificação

O linter agregado é o **golangci-lint 2.14.0**. O analisador estático dedicado é o **Staticcheck 2026.2.2**, que inclui suporte ao formato de exportação do Go 1.27.2. As versões ficam fixadas para que a análise seja reproduzível; o golangci-lint está configurado com verificações de erros, segurança, SQL e recursos modernos do Go. O conjunto próprio do Staticcheck roda separado, sem ser repetido dentro do golangci-lint.

Instale as versões fixadas uma vez:

```sh
go install github.com/golangci/golangci-lint/v2/cmd/golangci-lint@v2.14.0
go install honnef.co/go/tools/cmd/staticcheck@2026.2.2
```

Se os comandos não forem encontrados, inclua `$(go env GOPATH)/bin` no `PATH`.

Rode as verificações de análise junto dos testes:

```sh
golangci-lint run ./...
golangci-lint run --build-tags=integration ./...
staticcheck ./...
staticcheck -tags integration ./...
go test ./...
go test -race -tags integration ./...
CGO_ENABLED=0 GOOS=linux GOARCH=arm64 go build -trimpath -o bin/dianysma-arm64 ./cmd/dianysma
```

Os testes de integração criam e removem um PostgreSQL/pgvector descartável. Chamam todas as ferramentas pelo SDK MCP e validam as respostas contra os schemas publicados. O provedor de embeddings é um servidor TLS sintético usando o formato real do SDK Google; os testes não usam credenciais, bancos ou embeddings de produção.

Os cenários cobrem substituição de opcionais, vínculos e exclusões, concorrência na unicidade, ordenação semântica no pgvector, atualização do contexto indexado, cache por hash, reindexação, rollback por falha de provedor e cancelamento. Há verificações de TLS, limites HTTP, compatibilidade do protocolo e ausência de payloads nos logs.

## Container

```sh
docker buildx build --platform linux/arm64 -f Dockerfile.golang -t dianysma-go:local --load .
```

O build usa Go em uma etapa independente e gera um binário com `CGO_ENABLED=0`. A imagem final usa Docker Hardened Images, usuário sem privilégios, certificados CA e porta 8000. É necessário login no `dhi.io`, como na configuração existente. A pipeline atual continua usando o `Dockerfile` Java; a troca do deploy fica para a revisão da implementação Go.
