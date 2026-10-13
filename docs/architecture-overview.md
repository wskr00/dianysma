# Dianysma — arquitetura

**Versão:** 2.0 · **Data:** 12/10/2026<br>
**Direção:** servidor MCP privado de conhecimento profissional, acompanhado pela skill local `dianysma`.<br>
**Estado:** arquitetura em implementação; detalhes de domínio e infraestrutura ainda podem mudar.

## 1. Objetivo e escopo

O Dianysma armazena, organiza e recupera conhecimento profissional pessoal para agentes externos. O backend é um servidor MCP; o agente e a skill ficam no ambiente do cliente. O servidor não hospeda um agente LLM próprio.

O único domínio do backend é **Career Knowledge**: perfil, organizações, experiências, projetos, participações, claims, competências, formação, idiomas e suas projeções para busca vetorial.

O frontend Angular, a API REST de negócio e o módulo Job Discovery foram removidos. Pesquisas de vagas acontecem sob demanda no próprio agente, que pode comparar os resultados com fatos recuperados pelo MCP. Currículos e PDFs continuam sendo gerados localmente pela skill.

## 2. Componentes e fluxo

O diagrama fonte também está em [`architecture-components.mmd`](architecture-components.mmd).

```mermaid
flowchart LR
    subgraph CLIENT["Ambiente do usuário"]
        USER([Usuário])
        AGENT["Agente MCP externo"]
        SKILL["Skill dianysma\nSKILL.md + referências"]
        RENDER["render_resume.py + Typst"]
        PDF["PDF local"]
    end

    subgraph IDENTITY["Identidade"]
        COGNITO["Amazon Cognito\nOAuth 2.1 / OIDC"]
    end

    subgraph AWS["AWS · Amazon Bedrock AgentCore Runtime"]
        RUNTIME["JWT authorizer\nvalida o acesso MCP"]
        APP["Dianysma MCP Server\nSpring Boot 4 · Native Image ARM64\nporta 8000 · /mcp"]
        KNOWLEDGE["Módulo Career Knowledge"]
    end

    subgraph DATA["Persistência"]
        DB[("Neon PostgreSQL + pgvector")]
        VSTORE["vector_store\nPgVectorStore / Spring AI"]
        CHUNK["knowledge_chunk\nJPA + FKs canônicas"]
    end

    EMB["Gemini Embedding 2"]

    USER --> AGENT
    AGENT -. "segue instruções" .-> SKILL
    AGENT -->|"Authorization Code + PKCE"| COGNITO
    AGENT -->|"Bearer access token · MCP"| RUNTIME
    RUNTIME -->|"requisição autorizada"| APP
    APP --> KNOWLEDGE
    KNOWLEDGE --> DB
    KNOWLEDGE --> VSTORE
    KNOWLEDGE --> CHUNK
    VSTORE --> DB
    CHUNK --> DB
    VSTORE --> EMB
    AGENT --> RENDER
    SKILL -. "orienta seleção dos fatos" .-> AGENT
    RENDER --> PDF
    PDF --> USER
```

## 3. Limites de responsabilidade

| Componente | Responsabilidade |
| --- | --- |
| Agente externo | Pesquisar oportunidades com ferramentas próprias, usar o MCP e seguir as instruções da skill. |
| Skill `dianysma` | Orientar consultas e alterações de Career Knowledge, seleção de claims, preparação para entrevistas e composição de currículos. |
| `render_resume.py` + Typst | Gerar localmente um PDF a partir dos dados que o agente selecionou e estruturou. |
| AgentCore Runtime | Expor o runtime MCP e validar os JWTs de entrada conforme a configuração de OAuth. |
| Servidor Spring | Expor ferramentas MCP e aplicar os serviços e regras de Career Knowledge; não executa agentes nem pesquisa vagas. |
| Neon PostgreSQL | Manter entidades canônicas e as tabelas de indexação vetorial. |
| Gemini Embedding 2 | Gerar embeddings usados na busca semântica; não é um agente de chat hospedado pelo Dianysma. |

## 4. Organização do backend

O backend é uma aplicação Spring organizada por domínio, com o pacote `knowledge` para o Career Knowledge. O transporte MCP é a interface de entrada do sistema e chama os serviços de aplicação. Não há API REST de negócio.

As ferramentas MCP oferecem leitura, listagem, criação, atualização e exclusão dos registros de Career Knowledge quando aplicável. O perfil único não pode ser excluído; competências são um catálogo com busca e criação idempotente. Organizações e competências têm ferramentas de busca. A busca semântica cobre Claims, Participations com descrição e Educations; os demais registros são recuperados por suas ferramentas canônicas. RSQL JPA Specification não faz parte do build atual.

```text
src/main/java/io/github/wskr00/dianysma/
├── core/
│   └── audit/
└── knowledge/
    ├── controller/
    │   ├── dto/
    │   │   ├── command/
    │   │   │   ├── claim/
    │   │   │   ├── common/
    │   │   │   ├── education/
    │   │   │   ├── experience/
    │   │   │   ├── knowledge/
    │   │   │   ├── language/
    │   │   │   ├── organization/
    │   │   │   ├── participation/
    │   │   │   ├── profile/
    │   │   │   ├── project/
    │   │   │   └── skill/
    │   │   └── result/
    │   │       ├── claim/
    │   │       ├── common/
    │   │       ├── education/
    │   │       ├── experience/
    │   │       ├── knowledge/
    │   │       ├── language/
    │   │       ├── organization/
    │   │       ├── participation/
    │   │       ├── profile/
    │   │       ├── project/
    │   │       └── skill/
    │   └── mcp/
    ├── domain/
    │   └── enums/
    ├── repository/
    └── service/
        ├── exception/
        └── vector/
```

Os DTOs de entrada e saída ficam em `controller/dto`, separados por tipo e agregado. As exceções de aplicação ficam em `service/exception`; os serviços de aplicação ficam em `service`, e a integração com pgvector fica em `service/vector`. `core/audit` contém a superclasse `Auditable` compartilhada. A modelagem está detalhada em [`career-knowledge-class-diagram.md`](career-knowledge-class-diagram.md), e o contrato das ferramentas está em [`mcp-tool-contract.md`](mcp-tool-contract.md).

## 5. Career Knowledge e busca vetorial

O modelo de domínio permanece o descrito no diagrama de classes:

- `Profile` é o perfil pessoal único da instalação; `Language` se relaciona com ele.
- `Organization`, `Experience` e `Project` representam organizações, vínculos profissionais e projetos.
- `Participation` representa o contexto de uma atuação e pode se relacionar a uma experiência, um projeto ou ambos.
- `Claim` guarda fatos profissionais. Uma claim pode existir sem participação.
- `Skill` se relaciona com `Claim` por `claim_skill` (N:N).
- `Education` guarda formação, cursos e certificações.
- `KnowledgeChunk` relaciona uma fonte canônica com seu documento vetorial.

`KnowledgeVectorStore` é uma fachada por composição: usa o `PgVectorStore` do Spring AI para embeddings e similaridade e um repositório JPA para manter a projeção relacional. A busca semântica retorna o tipo e o ID da fonte; o agente chama a ferramenta MCP correspondente para recuperar o registro canônico e suas relações. A tabela `vector_store` contém `content`, `metadata` e `embedding`; `knowledge_chunk` contém o ID compartilhado com o vetor, a FK para exatamente uma fonte, o hash, o modelo e a data da indexação. A FK de `knowledge_chunk.id` para `vector_store.id` tem `ON DELETE CASCADE`. As fontes de domínio continuam sendo a verdade canônica.

O modelo de embedding decidido é Gemini Embedding 2 com 1536 dimensões, usando índice HNSW e distância cosseno no pgvector. O `PgVectorStore` terá `initialize-schema=false`; Flyway cria o schema técnico e as constraints PostgreSQL que JPA não expressa. A migração inicial continua em `V1__initial_schema.sql` enquanto o projeto ainda está em desenvolvimento; o banco local pode ser recriado quando essa V1 mudar.

## 6. Skill e fluxos locais

A skill é executada no cliente do agente e não no AgentCore. A estrutura alvo é:

```text
skill/
├── SKILL.md
├── references/
├── scripts/
│   └── render_resume.py
└── assets/
    └── resume.typ
```

O `SKILL.md` orienta o agente a recuperar e manter conhecimento pelo MCP, escolher claims verdadeiras para um currículo, apoiar preparação para entrevistas e executar a renderização local. A skill não acessa PostgreSQL diretamente e não contém mais funcionalidades de coleta de vagas.

**Estado do checkout:** não há atualmente uma pasta `skill/` versionada neste repositório. A arquitetura preserva essa capacidade como componente a ser incorporado; não havia arquivos da skill neste checkout para migrar.

## 7. Execução e infraestrutura

| Parte | Tecnologia / decisão atual |
| --- | --- |
| Runtime | Amazon Bedrock AgentCore Runtime |
| Aplicação | Java 25, Spring Boot 4.1.1, Spring AI |
| Artefato | GraalVM Native Image para ARM64; container escuta na porta 8000 |
| Transporte MCP | Stateless Streamable HTTP em `/mcp` |
| Identidade | Amazon Cognito como authorization server OAuth/OIDC; login Google pode ser federado |
| Banco | Neon PostgreSQL com pgvector |
| Embeddings | Gemini Embedding 2, 1536 dimensões |
| Filtros de consulta | Busca por nome em organizações e competências; consulta semântica para fatos, participações e formações |
| Desenvolvimento local | Docker Compose com PostgreSQL e pgvector |

O container do AgentCore chama o servidor Spring na porta 8000. O banco permanece externo no Neon. `compose.yaml` é somente para o banco local; não hospeda backend nem frontend.

### Integração com AWS

A aplicação Java não autentica tokens. O Cognito emite tokens OAuth e o AgentCore Runtime V2 valida cada invocação com `customJWTAuthorizer` antes de encaminhar o tráfego ao servidor MCP.

## 8. Segurança e autenticação

O fluxo previsto é:

1. O cliente MCP inicia Authorization Code com PKCE no Cognito, solicitando o recurso MCP e o scope `knowledge.access`.
2. Cognito autentica a conta permitida e emite um access token destinado ao recurso MCP, com o scope `<RESOURCE_SERVER_IDENTIFIER>/knowledge.access`.
3. O cliente envia o bearer token ao endpoint MCP do AgentCore Runtime.
4. O `customJWTAuthorizer` valida assinatura e validade do JWT, issuer pela URL de discovery, audience, `client_id` e scope permitido. Somente então o Runtime encaminha a chamada ao container.

Configure o App Client para permitir somente o scope customizado `knowledge.access` do Resource Server. Use como identificador do Resource Server a URI de invocação do AgentCore sem `?qualifier=DEFAULT`; o valor completo do scope passa a ser `<RESOURCE_SERVER_IDENTIFIER>/knowledge.access`. Configure o authorizer do Runtime com a URL OIDC `https://cognito-idp.<region>.amazonaws.com/<user-pool-id>/.well-known/openid-configuration`, audience igual ao valor `aud` emitido para esse recurso, o Client ID exato do App Client em `allowedClients` e o scope completo em `allowedScopes`. Os campos configurados são cumulativos: quando definidos, audience, client e scope precisam corresponder.

O formato esperado no Runtime é:

```json
{
  "customJWTAuthorizer": {
    "discoveryUrl": "https://cognito-idp.<region>.amazonaws.com/<user-pool-id>/.well-known/openid-configuration",
    "allowedAudience": ["<RESOURCE_SERVER_IDENTIFIER>"],
    "allowedClients": ["<COGNITO_APP_CLIENT_ID>"],
    "allowedScopes": ["<RESOURCE_SERVER_IDENTIFIER>/knowledge.access"]
  }
}
```

O App Client identifica uma aplicação, não necessariamente uma pessoa. Restrinja o User Pool e os fluxos de login à conta pretendida. Se outras pessoas puderem obter tokens com esse mesmo Client ID, acrescente uma condição de identidade em `customClaims` (por exemplo, um claim estável do usuário) e não dependa de `allowedClients` como identidade individual. O endpoint do container deve aceitar tráfego somente da infraestrutura do Runtime ou ficar em rede privada; não o exponha diretamente sem proteção equivalente. Não habilite modo anônimo no Runtime. Não há validação JWT nem autorização por usuário dentro da aplicação.

O Spring AI atende `ping`, `initialize`, `tools/list` e `tools/call` em `POST /mcp` usando transporte Stateless Streamable HTTP. O endpoint MCP não tem exceção local de health check: a autenticação de entrada acontece antes, no AgentCore.

## 9. Compatibilidade MCP

O alvo atual é MCP `2025-11-25`, compatível com a linha MCP Java SDK 2.x usada pelo Spring AI 2.x. O servidor usará Stateless Streamable HTTP: cada chamada é independente e não mantém sessão; recursos bidirecionais como sampling e elicitation não fazem parte do desenho atual.

A especificação MCP `2026-07-28` é posterior ao alvo suportado pela stack escolhida. Não implementaremos o protocolo manualmente nem anunciaremos suporte a essa versão. A interoperabilidade entre ChatGPT, AgentCore Runtime e Spring AI precisa ser validada antes da implantação; se um cliente exigir operações fora do suporte do SDK 2.x, isso será uma decisão de compatibilidade separada.

## 10. Configuração local

Copie `.env.example` para `.env` e ajuste as credenciais locais. O Compose lê `DATABASE_NAME`, `DATABASE_USER` e `DATABASE_PASSWORD`; a aplicação lê também `DATABASE_HOST`, `DATABASE_PORT` e `GEMINI_API_KEY`. Cognito e AgentCore são configurados fora do processo Java; não há issuer, audience ou scope de autenticação no `application.yml`.

## 11. Decisões ainda abertas

- Ferramentas MCP para os demais agregados de Career Knowledge e os limites de seus fluxos de edição.
- Comportamento de OAuth Cognito/AgentCore com ChatGPT e outros clientes MCP, incluindo descoberta e configuração de audience/scopes.
- Provisionamento e recuperação segura de segredos no runtime.
- Estratégia de reindexação retomável e consistência transacional entre JPA e `PgVectorStore`.
- Pipeline de build e publicação do container nativo ARM64 para AgentCore.
- Estrutura e conteúdo versionados da skill `dianysma`.

## 12. Documentos relacionados

- [`career-knowledge-class-diagram.md`](career-knowledge-class-diagram.md) — entidades, regras e persistência vetorial.
- [`architecture-components.mmd`](architecture-components.mmd) — diagrama de componentes editável.
