# Contrato das ferramentas MCP

**Protocolo:** MCP `2025-11-25`, Stateless Streamable HTTP.
**Escopo:** operações do domínio Career Knowledge, sem API REST de negócio.

## Princípios

- As ferramentas expõem operações pequenas que o agente pode escolher. O servidor não executa um fluxo de agente nem encadeia chamadas de LLM.
- Os nomes estáveis das ferramentas ficam em inglês; títulos, descrições, instruções de parâmetros e mensagens de erro ficam em português.
- Leitura, listagem, criação, atualização e exclusão são operações distintas. Não há `upsert` genérico para registros de conhecimento.
- As entradas passam por Bean Validation nos command DTOs e pelas regras de domínio nos serviços. Os limites também aparecem nas descrições MCP, pois o gerador de schema do Spring AI não converte automaticamente todas as constraints Bean Validation.
- As respostas são DTOs, nunca entidades JPA. Busca semântica retorna IDs estáveis para que o agente leia o registro canônico antes de usá-lo ou alterá-lo.
- Campos opcionais nas respostas aceitam `null` no schema MCP, de acordo com a nulabilidade das entidades; isso evita rejeitar uma resposta depois de a operação já ter persistido a alteração.
- Listagens e buscas retornam um objeto estruturado com uma coleção nomeada: `claims`, `educations`, `experiences`, `languages`, `organizations`, `participations`, `projects`, `results` ou `skills`. A coleção pode estar vazia.
- Os serviços verificam escopos de leitura e escrita. As hints MCP descrevem a operação para o cliente, mas não substituem autorização no servidor.

## Ferramentas

| Domínio | Ferramentas | Regras principais |
| --- | --- | --- |
| Busca semântica | `search_knowledge` | Consulta obrigatória, até 2.000 caracteres; limite padrão 5, intervalo 1–10. Retorna `sourceType`, `sourceId`, trecho de até 500 caracteres e similaridade. O score ordena resultados; não representa confiança factual. |
| Perfil | `get_profile`, `create_profile`, `update_profile` | Existe no máximo um perfil por instalação. `create_profile` falha se ele já existir. `update_profile` substitui todos os campos; campos omitidos ou nulos são limpos. Não há exclusão do perfil. |
| Organizações | `get_organization`, `search_organizations`, `create_organization`, `update_organization`, `delete_organization` | A busca é por trecho do nome, limite padrão 10 e máximo 50. Antes de criar, pesquise para reutilizar uma organização existente. A exclusão é recusada enquanto houver experiência ou projeto associado. |
| Experiências | `get_experience`, `list_experiences`, `create_experience`, `update_experience`, `delete_experience` | Empregadora obrigatória; cliente opcional. Se as duas datas forem informadas, `endedAt` não pode anteceder `startedAt`. A exclusão é recusada enquanto houver participações associadas. Listas usam limite padrão 20 e máximo 50. |
| Projetos | `get_project`, `list_projects`, `create_project`, `update_project`, `delete_project` | Organização opcional. A exclusão é recusada enquanto houver participações associadas. Listas usam limite padrão 20 e máximo 50. |
| Participações | `get_participation`, `list_participations`, `create_participation`, `update_participation`, `delete_participation` | Informe uma experiência, um projeto ou ambos. A descrição pode ter até 4.000 caracteres. A combinação é única, inclusive quando um dos vínculos é nulo. A exclusão é recusada enquanto houver fatos profissionais associados. Listas usam limite padrão 20 e máximo 50. |
| Fatos profissionais | `get_claim`, `list_claims`, `create_claim`, `update_claim`, `delete_claim` | O conteúdo pode ter até 4.000 caracteres. `list_claims` aceita `participationId` opcional e limite padrão 20, máximo 50. Uma Claim pode ser autônoma. Em `update_claim`, vínculos omitidos são preservados; use `clearParticipation=true` para desassociar e `skillIds=[]` para remover competências. |
| Competências | `search_skills`, `create_skill` | Catálogo canônico. `create_skill` retorna a competência existente quando o nome já existe sem diferenciar maiúsculas de minúsculas; aliases novos não alteram o registro existente. |
| Formações | `get_education`, `list_education`, `create_education`, `update_education`, `delete_education` | O tipo é `CERTIFICATION`, `COURSE` ou `DEGREE`; carga horária não pode ser negativa; a data de conclusão não pode anteceder a inicial quando ambas são informadas. A descrição pode ter até 4.000 caracteres. Listas usam limite padrão 20 e máximo 50. Criar e atualizar sincronizam a projeção vetorial; excluir remove a projeção. |
| Idiomas | `get_language`, `list_languages`, `create_language`, `update_language`, `delete_language` | Cada idioma pertence ao perfil único; crie o perfil primeiro. Proficiência: `BASIC`, `INTERMEDIATE`, `ADVANCED`, `FLUENT` ou `NATIVE`. Listas usam limite padrão 20 e máximo 50. |

Nas atualizações de experiência, projeto, participação, formação e idioma, os campos obrigatórios substituem os valores atuais. Quando houver campos opcionais, omiti-los ou enviá-los como nulos os limpa. Consulte o registro antes de atualizar quando precisar preservar algum valor. Exclusões de registros inexistentes retornam `deleted=false`; elas são idempotentes.

## Sequência de uso

- Consulte `get_profile` antes de criar o perfil; crie-o antes de adicionar idiomas.
- Pesquise organizações antes de criá-las. Crie ou localize a empregadora e o cliente antes de registrar uma experiência; crie ou localize a organização antes de associá-la a um projeto.
- Registre a experiência e/ou o projeto antes da participação. A participação exige ao menos um desses vínculos.
- Pesquise competências antes de criar uma nova ou associá-la a um fato. Crie a participação e as competências antes de criar o fato, se ele precisar desses vínculos.
- Use `search_knowledge` para encontrar conteúdo semanticamente semelhante. Os resultados são trechos; leia o registro canônico pela ferramenta `get_*` correspondente antes de decidir uma atualização.

## Hints de comportamento

As ferramentas de consulta e busca declaram `readOnlyHint=true`; as ferramentas de escrita declaram `readOnlyHint=false`. Criações declaram `destructiveHint=false` porque apenas acrescentam registros. Atualizações e exclusões declaram `destructiveHint=true`: atualizações podem substituir valores existentes ou limpar campos omitidos, e exclusões removem registros. Confirmações ou avisos exibidos pelo cliente para essas operações são esperados. Essas annotations são hints para o cliente, não controles de autorização; a proteção efetiva permanece nos escopos exigidos pelo servidor.

O catálogo de competências não oferece atualização ou exclusão nesta versão: renomear uma competência altera os embeddings das Claims associadas, e removê-la pode apagar vínculos profissionais. Uma futura operação de manutenção deverá reindexar os fatos relacionados e tratar vínculos explicitamente.

Por padrão, `dianysma/knowledge.read` é exigido nas ferramentas de consulta, busca e listagem, e `dianysma/knowledge.write` para criação, atualização e exclusão. Os valores são configuráveis por `KNOWLEDGE_READ_SCOPE` e `KNOWLEDGE_WRITE_SCOPE`; devem corresponder exatamente aos scopes emitidos no claim `scope` do access token. O conversor JWT mapeia esses scopes para authorities internas, que o filtro HTTP e cada serviço verificam; um token somente de leitura não autoriza escrita e vice-versa.

## Busca e projeção vetorial

- `search_knowledge` pesquisa Claims, Participations com descrição e Educations. A resposta agrupa os resultados em `results`; `sourceType` informa `claim`, `participation` ou `education`, e `sourceId` é o UUID do registro canônico.
- Use `get_claim`, `get_participation` ou `get_education`, conforme `sourceType`, para recuperar o estado canônico e suas relações.
- Cada documento usa como ID o UUID da fonte e metadata `sourceType` e `sourceId`. A Claim inclui conteúdo e competências; a Participation inclui descrição, cargo/organização e projeto; a Education inclui título, instituição, tipo, grau e descrição.
- Uma Participation sem descrição não gera documento vetorial. Alterações no texto projetado recalculam o embedding; mudanças que não alterem esse texto não geram embedding novamente.
- Consultas recebem o prefixo `task: search result | query: ` antes do embedding, conforme o formato adotado para Gemini Embedding 2.
- O modelo aceita até 8.192 tokens por entrada. Os campos textuais indexados são limitados a 4.000 caracteres, e o texto combinado da fonte passa por uma barreira adicional de 6.000 pontos de código antes de chamar o provedor; essa barreira reduz risco de exceder o limite, sem substituir a contagem exata de tokens do modelo.
- `knowledge_chunk.id` referencia `vector_store.id` com `ON DELETE CASCADE`. A escrita do documento e do chunk usa o mesmo DataSource e a transação da operação de domínio. A chamada ao provedor de embeddings pode ser repetida se a transação falhar.
- `vector_store` é uma projeção derivada. As entidades e relações JPA permanecem como fonte canônica.

## Erros e segurança

- Entradas inválidas e referências inexistentes retornam mensagens acionáveis em português.
- Falhas inesperadas de persistência ou embedding retornam mensagem genérica ao cliente; detalhes internos são registrados no log do servidor.
- O endpoint MCP exige um dos escopos de conhecimento, e cada serviço exige o escopo específico da operação.
- Cognito e o authorizer do AgentCore restringem quais identidades podem obter e usar os tokens aceitos pelo serviço.

## Recursos e prompts

As operações de conhecimento são chamadas explícitas a ferramentas. O servidor não publica Resources ou Prompts redundantes neste momento; esses primitivos podem ser adicionados quando houver contexto endereçável ou fluxo reutilizável que não seja atendido pelas ferramentas existentes.

## Referências

- [MCP 2025-11-25 — Tools](https://modelcontextprotocol.io/specification/2025-11-25/server/tools)
- [MCP 2025-11-25 — Resources](https://modelcontextprotocol.io/specification/2025-11-25/server/resources)
- [MCP 2025-11-25 — Prompts](https://modelcontextprotocol.io/specification/2025-11-25/server/prompts)
- [Spring AI 2.0.1 — MCP Server Annotations](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-annotations-server.html)
- [Spring AI 2.0.1 — PgVectorStore API](https://docs.spring.io/spring-ai/docs/current/api/org/springframework/ai/vectorstore/pgvector/PgVectorStore.html)
