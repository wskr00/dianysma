# Preparar currículos

Use este fluxo quando o usuário pedir para criar, adaptar ou revisar um currículo com base no Dianysma. Ele coordena a recuperação das evidências e a entrega; consulte [`resume-writing.md`](resume-writing.md) para critérios de redação, [`retrieval-and-research.md`](retrieval-and-research.md) para busca e pesquisa, e [`knowledge-capture.md`](knowledge-capture.md) somente se o usuário também pedir para registrar ou corrigir fatos.

## Definir o objetivo

- Identifique se o pedido é criar, adaptar ou revisar; determine o cargo ou vaga-alvo, o idioma, o formato e as restrições de extensão ou conteúdo.
- Se faltar uma informação que altere a seleção ou o resultado, pergunte. Não presuma um cargo ou objetivo profissional quando o usuário não informou.
- Para uma vaga fornecida, extraia os requisitos e responsabilidades relevantes antes de buscar evidências. Pesquisa externa sobre uma vaga ou empresa segue [`retrieval-and-research.md`](retrieval-and-research.md); mantenha esses dados separados do histórico do usuário.

## Recuperar e selecionar evidências

1. Consulte o `Profile` para dados pessoais disponíveis e recupere os registros relevantes ao objetivo. Em um currículo que precisa cobrir o histórico de forma ampla, use também as ferramentas de listagem: `search_knowledge` retorna resultados relevantes, não um inventário completo.
2. Para cada requisito importante, formule consultas específicas em `search_knowledge`. Use `sourceType` e `sourceId` para ler cada fonte canônica antes de usar seus fatos; confirme contexto, período e vínculos em `Experience`, `Participation`, `Project` e `Claim`.
3. Inclua formação, competências e idiomas quando forem pertinentes e estiverem sustentados pelos registros. Ausência de resultado na busca ou no banco significa “não encontrado”, não que a pessoa não tenha a experiência.
4. Para uma vaga-alvo, relacione cada requisito relevante à evidência direta, parcial ou não encontrada. Use essa relação para escolher conteúdo; não a apresente como uma pontuação garantida de ATS.
5. Selecione evidências relevantes e distintas. Não tente incluir todo o histórico nem repita o mesmo fato em várias seções para preencher espaço.

## Compor o currículo

- Organize as seções conforme o objetivo e as evidências disponíveis. O asset visual não torna seus campos de exemplo obrigatórios.
- Adapte a ordem e a ênfase ao objetivo, sem alterar o sentido dos fatos. Siga [`resume-writing.md`](resume-writing.md) para redigir itens específicos, tratar resultados e métricas, e conferir termos da vaga.
- Use apenas dados de contato disponíveis no perfil ou fornecidos pelo usuário. Omita os desconhecidos; não mantenha nomes, contatos, cargos ou fatos fictícios do asset.
- Se faltar evidência importante, omita a afirmação e, quando isso afetar o resultado, informe a lacuna ao usuário ou faça uma pergunta objetiva.

## Gerar e conferir o arquivo

1. Para criar HTML, leia [`assets/resume-style-reference.html`](../assets/resume-style-reference.html) e use-o como ponto de partida visual e semântico. Substitua todo o conteúdo fictício e adapte as seções ao currículo; ele é uma referência, não um formulário rígido.
2. Preserve a base visual do asset: página A4 branca, uma coluna, cabeçalho centralizado e hierarquia tipográfica simples. Ajuste as seções, o espaçamento e as quebras ao conteúdo real; não reduza a legibilidade para forçar uma quantidade de páginas.
3. A skill não inclui script próprio de renderização. Produza ou adapte o HTML diretamente e, quando o usuário pedir PDF, use os recursos de geração disponíveis no ambiente, como a skill `$pdf:pdf` quando disponível. Se não houver recurso para gerar PDF, entregue HTML pronto para impressão e explique essa limitação.
4. Confira o documento final, não apenas o HTML: verifique texto, links, datas, paginação, quebras e legibilidade em todas as páginas. Corrija conteúdo cortado ou divisão que prejudique a leitura antes de entregar.

## Revisão final

- Compare cada afirmação importante com sua fonte canônica ou com algo confirmado pelo usuário.
- Confira nomes, cargos, períodos, competências e dados de contato; não deixe conteúdo fictício nem links de exemplo.
- Confirme que a seleção corresponde ao objetivo e às restrições pedidas. Ao entregar, sinalize lacunas relevantes fora do currículo sem preenchê-las com suposições.
