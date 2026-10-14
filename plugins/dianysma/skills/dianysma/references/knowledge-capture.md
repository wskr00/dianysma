# Capturar conhecimento profissional

Use esta referência quando o usuário pedir para registrar ou corrigir informações profissionais no Dianysma. Consulte [`knowledge-model.md`](knowledge-model.md) para escolher os registros e vínculos; para transformar fatos em texto de currículo, consulte [`resume-writing.md`](resume-writing.md).

## Elicitar fatos úteis

- Trate o relato do usuário como fonte. Separe o que foi afirmado do que seria apenas uma interpretação; não complete lacunas por inferência.
- Para cada atuação que possa ser reutilizada, identifique o que estiver disponível sobre contexto ou problema, papel pessoal, ações ou decisões, entrega, resultado ou outra evidência, escala e ferramentas. Isso é um guia para entender o relato, não uma lista de campos obrigatórios.
- Faça perguntas naturais e pontuais. Priorize o detalhe que mais mudaria a precisão ou a utilidade do registro, em vez de aplicar um questionário a cada experiência.
- Métricas são opcionais. Se não houver número conhecido, registre uma entrega, responsabilidade, decisão, restrição atendida ou outro resultado confirmado. Não transforme objetivo em resultado nem estimativa em medida comprovada.
- Diferencie a contribuição pessoal do trabalho da equipe. Preserve qualificadores como “aproximadamente” quando o usuário os informar e mantenha contexto suficiente para interpretar números, datas e escopo.
- Se um dado opcional não for conhecido e não for necessário para classificar ou relacionar o registro, deixe-o ausente. Não interrompa a captura para preencher todos os campos.

## Modelar o relato

- Use `Experience` para o vínculo profissional, `Project` para um projeto com identidade própria e `Participation` para o contexto de atuação ligado a uma experiência, projeto ou ambos. Não crie uma experiência ou projeto por tarefa.
- Use `Participation.description` para resumir o escopo ou as responsabilidades naquele contexto. Use `Claim` para um fato profissional específico e reutilizável, como uma entrega, contribuição, responsabilidade ou resultado.
- Escreva cada `Claim` como uma afirmação factual que possa ser entendida isoladamente. A busca semântica indexa o conteúdo da claim e os nomes das competências, sem incorporar a descrição da participação; inclua no conteúdo o contexto essencial para encontrá-la e interpretá-la. Quando conhecido, associe também o `participationId` para preservar o vínculo canônico.
- Associe `Skill`s existentes às claims que sustentam seu uso. Uma tecnologia mencionada sem contexto não comprova, por si só, proficiência; o Dianysma não armazena nível de proficiência em `Skill`.
- Uma informação pode completar um registro existente. Evite duplicatas, mas não conclua que dois registros são iguais apenas por terem nomes ou títulos parecidos.

## Preparar e conferir a gravação

1. Consulte os registros relacionados antes de criar. Use `search_knowledge` para localizar fatos semanticamente próximos e as ferramentas de listagem ou leitura quando precisar confirmar o histórico canônico. A busca retorna resultados relevantes, não uma garantia de que todos os semelhantes foram encontrados.
2. Se não for possível distinguir com segurança entre um registro novo e um existente, ou se o vínculo correto estiver incerto, pergunte antes de gravar.
3. Crie primeiro os registros necessários às relações e use os IDs devolvidos pelas ferramentas MCP. Siga [`knowledge-model.md`](knowledge-model.md); não crie registros artificiais para satisfazer uma associação.
4. Pesquise o catálogo antes de criar uma competência. `create_skill` pode devolver a competência existente sem acrescentar aliases enviados junto com a solicitação.
5. Depois de gravar, confira o retorno, os fatos e as relações solicitadas. Confirme ao usuário o que foi registrado e indique informações importantes que permaneceram desconhecidas.

## Atualizar sem perder dados

Antes de atualizar, leia a versão canônica completa e confira a descrição e o schema da ferramenta MCP. As operações não têm todas a mesma semântica:

- `update_profile` substitui o perfil inteiro. Reenvie os valores que devem ser mantidos.
- Em experiências, projetos, participações, formações e idiomas, campos opcionais omitidos ou nulos podem ser limpos. Preserve explicitamente os valores que o usuário não pediu para remover.
- `update_claim` substitui o conteúdo, mas preserva vínculos omitidos; `clearParticipation=true` remove a participação e `skillIds=[]` remove as competências associadas.

Faça somente as alterações solicitadas. Se o relato corrigir um fato e também acrescentar outro, aplique cada mudança ao registro apropriado; não reescreva conteúdo relacionado sem autorização. Depois, confira o resultado e confirme quais campos e relações foram alterados.
