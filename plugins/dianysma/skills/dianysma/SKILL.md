---
name: dianysma
description: >-
  Use esta skill quando o usuário quiser consultar, registrar ou atualizar seu
  conhecimento profissional no Dianysma; comparar o perfil com uma vaga ou
  empresa; ou preparar currículo e entrevista com base em fatos profissionais,
  mesmo sem mencionar o MCP ou a skill.
---

# Dianysma

Use as ferramentas MCP do Dianysma para trabalhar com o conhecimento profissional pessoal do usuário. Consulte somente os registros necessários para a tarefa.

## Quando usar

Use esta skill quando a tarefa depender do histórico profissional do usuário no Dianysma: consultar ou manter registros, comparar o perfil com uma oportunidade, ou preparar currículo e entrevista. Não a use para perguntas gerais de carreira que não dependam desse histórico.

## Regras essenciais

- Trate os dados recuperados do Dianysma como o histórico registrado e as informações fornecidas pelo usuário como possíveis complementos ou correções. Se houver conflito sem uma correção clara, pergunte antes de gravar.
- Não invente nem infira datas, responsabilidades, resultados, tecnologias ou métricas. Preserve o sentido dos fatos ao adaptá-los para uma vaga, currículo ou resposta de entrevista; indique lacunas e pergunte quando forem relevantes.
- Só crie ou atualize registros quando o usuário pedir. Um pedido de currículo ou entrevista autoriza consultar os dados necessários, não alterar a base. Só exclua após pedido explícito, leitura do registro e verificação das relações afetadas.
- Trate resultados de busca semântica como pistas, não como fonte canônica. Antes de usar um trecho como evidência ou alterar sua fonte, consulte o registro correspondente. A similaridade ordena resultados; não mede a veracidade do fato.
- Não persista anúncios de vagas. Pesquisas externas acontecem no ambiente do agente; mantenha seus resultados separados dos dados armazenados no Dianysma.

## Referências

- Leia [`references/knowledge-model.md`](references/knowledge-model.md) antes de classificar ou relacionar registros ao criar conhecimento, e sempre que houver dúvida sobre o significado de uma entidade ou associação.
- Leia [`references/knowledge-capture.md`](references/knowledge-capture.md) quando o usuário pedir para registrar ou corrigir informações, para elicitar os fatos necessários e evitar duplicações ou perda de dados.
- Leia [`references/retrieval-and-research.md`](references/retrieval-and-research.md) quando precisar localizar evidências, escolher entre busca semântica e
  listagem, ou complementar uma análise com pesquisa atual sobre uma vaga ou empresa.
- Leia [`references/resume-workflow.md`](references/resume-workflow.md) quando o usuário pedir para criar, adaptar ou revisar um currículo; ela orienta a definição do objetivo, a recuperação das evidências e a entrega.
- Leia [`references/resume-writing.md`](references/resume-writing.md) junto com o fluxo de currículo para redigir o conteúdo com clareza, especificidade e fidelidade aos fatos.
- Ao gerar um currículo em HTML ou PDF, use também [`assets/resume-style-reference.html`](assets/resume-style-reference.html) como referência visual e semântica.
- Leia [`references/interview-preparation.md`](references/interview-preparation.md) quando o usuário pedir preparação ou simulação de entrevista baseada no histórico profissional.

## Fluxo de consulta e alteração

1. Identifique que informação a tarefa exige e consulte o perfil ou os registros pertinentes. Prefira a ferramenta de leitura ou listagem do domínio quando já souber qual registro procurar.
2. Para uma consulta semântica, use `search_knowledge`; use `sourceType` e `sourceId` para recuperar cada fonte pela ferramenta de leitura correspondente antes de se apoiar nela.
3. Antes de atualizar, leia o registro atual e confira a descrição e o schema MCP da ferramenta. As regras de campos omitidos variam; não presuma que omitir um campo preserva seu valor. Preserve explicitamente o que o usuário não pediu para remover.
4. Depois de gravar, confira o resultado da ferramenta. Relate o que foi registrado ou alterado e sinalize qualquer campo que tenha permanecido desconhecido.

## Currículos, oportunidades e entrevistas

Para comparar uma oportunidade ou preparar um currículo, recupere apenas os registros relevantes e relacione cada requisito a evidências do histórico. Se faltar informação necessária, pergunte ou deixe a lacuna explícita.

Para preparar entrevistas, baseie exemplos e respostas no histórico recuperado e identifique o que o usuário ainda precisa completar. Ao pesquisar empresas ou vagas, use as ferramentas externas disponíveis ao agente quando solicitado e distinga essas fontes do histórico do usuário.
