# Planejamento da Skill Dianysma

**Estado:** instruções, referências e referência visual HTML implementadas.

## Propósito e limites

A skill `dianysma` orientará agentes a consultar e manter o conhecimento profissional pessoal pelo MCP e a usar esses fatos em fluxos de carreira, como pesquisa contextual, preparação para entrevistas e composição de currículos. É uma unidade coerente porque esses fluxos partem da mesma base profissional.

O MCP fornece operações e registros estruturados; a skill ensina o agente a escolher e encadear essas operações. Ela não deve duplicar schemas, validações já explicadas pelas ferramentas, nem acessar o banco diretamente. Pesquisas de vagas podem ocorrer no ambiente do agente quando solicitadas, mas anúncios não são persistidos no backend.

## Princípios de comportamento

- Basear o conteúdo em fatos recuperados ou fornecidos pelo usuário. Não inventar datas, responsabilidades, resultados, tecnologias ou métricas; identificar lacunas e pedir esclarecimento quando necessário.
- Distinguir `Experience` (vínculo profissional), `Project` (projeto), `Participation` (atuação em um vínculo e/ou projeto) e `Claim` (fato profissional, associado ou não a uma participação).
- Antes de criar organizações ou competências, pesquisar para reutilizar registros existentes. Antes de alterar um registro, consultar a versão canônica e preservar relações intencionalmente.
- Tratar resultados de busca semântica como pistas, não como registros completos: recuperar a fonte canônica e o contexto pelas ferramentas MCP antes de tomar decisões ou editar.
- Na pesquisa externa, separar claramente informação externa dos dados do Dianysma. Ao preparar currículo ou entrevista, adaptar a apresentação sem mudar o sentido dos fatos.

## Formato e localização

A skill local do Codex ficará em `.agents/skills/dianysma/`. O nome da pasta deve corresponder ao `name` do frontmatter, que será `dianysma`. A estrutura interna segue o formato portátil Agent Skills:

```text
.agents/skills/dianysma/
├── SKILL.md
└── references/
    ├── knowledge-model.md
    ├── knowledge-capture.md
    ├── retrieval-and-research.md
    ├── resume-workflow.md
    └── interview-preparation.md
└── assets/
    └── resume-style-reference.html
```

O `SKILL.md` terá frontmatter YAML válido com `name` e `description`. O nome precisa ter até 64 caracteres, usar apenas letras minúsculas, números e hífens, sem hífens consecutivos ou nas extremidades, e corresponder ao nome da pasta. A descrição não pode ser vazia, terá até 1024 caracteres e explicará o que a skill faz e quando usá-la. Ela será escrita em termos da intenção do usuário, com um gatilho direto como “Use esta skill quando...” e contemplará pedidos implícitos, não só os que citam Dianysma.

Campos opcionais só serão incluídos se houver necessidade concreta; `compatibility` só se houver requisito real de ambiente, e `allowed-tools` é experimental e não será usado sem necessidade e suporte confirmados.

## Conteúdo e carregamento progressivo

O corpo de `SKILL.md` conterá apenas as instruções centrais necessárias em toda ativação: objetivo, gatilhos e limites, fluxo básico, gotchas importantes e indicação explícita de qual referência consultar para cada tarefa. Será procedural, com padrões claros e concisos; evitará explicações genéricas que o agente já conhece e não tentará cobrir todos os casos hipotéticos.

O documento principal deve ficar abaixo de 500 linhas e mirar menos de 5.000 tokens, conforme as recomendações da especificação. Esses limites são guias para manter o contexto útil, não metas de preenchimento ou substitutos para julgar se cada instrução realmente ajuda. Referências serão focadas, citadas por caminhos relativos à raiz da skill e mantidas a um nível de profundidade a partir do `SKILL.md`.

As referências previstas terão responsabilidades distintas:

- `knowledge-model.md`: entidades, significado e relações do Career Knowledge.
- `knowledge-capture.md`: como elicitar, pesquisar, criar e atualizar registros sem duplicar nem perder contexto.
- `retrieval-and-research.md`: busca semântica, leitura canônica e separação entre dados armazenados e pesquisa externa.
- `resume-workflow.md`: seleção e apresentação fiel de fatos para currículos.
- `interview-preparation.md`: preparação de perguntas e respostas apoiada no conhecimento registrado.

Cada referência será carregada apenas quando seu fluxo for pertinente; o `SKILL.md` dirá claramente quando fazê-lo. Schemas e regras mecânicas das ferramentas continuarão no contrato MCP. Detalhes não óbvios que o agente precisa saber antes de agir — como a busca semântica retornar trechos, ou `Claim` poder existir sem `Participation` — ficarão nas instruções centrais ou na referência indicada, conforme a frequência e o risco do erro. As referências da skill serão resumos úteis para o agente, derivados da documentação do projeto, e não cópias extensas do diagrama ou do contrato MCP.

Exemplos curtos e checklists serão usados quando esclarecerem uma decisão ou ajudarem a não pular etapas dependentes. Para ações em lote ou destrutivas, o fluxo deve prever um plano e uma verificação contra os registros canônicos antes da execução.

## Como construir e refinar

O conteúdo será extraído das decisões e correções reais do projeto, do [diagrama de Career Knowledge](career-knowledge-class-diagram.md), do [contrato das ferramentas MCP](mcp-tool-contract.md) e de execuções reais. A skill deve registrar conhecimento específico do Dianysma, não conselhos genéricos de carreira. Instruções prescritivas serão usadas onde a sequência ou a integridade dos dados importa; para escolhas de apresentação, a skill dará critérios e um padrão preferido sem eliminar o julgamento do agente.

Depois de uma primeira versão, serão observadas execuções completas e seus passos, não apenas a resposta final. Acionamentos indevidos, etapas improdutivas, omissões e correções do usuário orientarão mudanças em descrição, instruções, exemplos e gotchas.

## Validação planejada

1. Executar `skills-ref validate` na pasta da skill para verificar frontmatter e convenções de nome.
2. Avaliar a descrição com cerca de 16–20 prompts realistas: 8–10 que devem acionar a skill e 8–10 casos próximos que não devem. Variar linguagem formal e casual, erros/abreviações, pedidos curtos e contextuais, e gatilhos explícitos e implícitos; observar a invocação no cliente usado.
3. Se a descrição for ajustada, manter conjuntos de treino e validação separados (aproximadamente 60/40), evitar otimizar para palavras específicas dos prompts e escolher a versão pelo desempenho de validação. Se o acionamento variar entre execuções, repetir os casos (três execuções são um ponto de partida razoável) e comparar taxas de acionamento.
4. Após selecionar a descrição, conferir a generalização com 5–10 prompts novos que não participaram da otimização.
5. Com a skill ativada, verificar os fluxos relevantes: relações do modelo preservadas, fonte canônica consultada antes de alterações, fatos sem suporte não inventados e pesquisas externas distinguidas dos dados persistidos. Examinar a execução e os passos, não apenas a resposta final.

## Currículo e apresentação visual

O asset HTML é uma referência visual e semântica, com dados fictícios; não fixa campos nem substitui a seleção de evidências. O agente adapta as seções ao objetivo e ao histórico, e gera PDF com os recursos disponíveis no ambiente — por exemplo, a skill `$pdf:pdf` no ChatGPT. Se não houver geração de PDF, pode entregar o HTML pronto para impressão. Não há script próprio de renderização. A pasta `skill/` da raiz não é usada; `.agents/skills/dianysma/` é a fonte de verdade da skill local do Codex.
