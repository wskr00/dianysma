# Dianysma — Diagrama de Classes: Career Knowledge

**Versão:** 1.1 · **Data:** 10/10/2026  
**Escopo:** primeiro módulo (conhecimento profissional pessoal), incluindo perfil, duas tabelas de indexação vetorial e composição sobre Spring AI PgVectorStore.  
**Formato:** Mermaid `classDiagram` com tipos no estilo Java e campos persistidos em PostgreSQL. `VectorDocument` representa a tabela física `vector_store` do Spring AI; as classes de serviço e repositório representam comportamento, não tabelas adicionais.

## Diagrama

```mermaid
classDiagram
    direction LR

    class Profile {
        +UUID id
        +String fullName
        +String email
        +String phone
        +String city
        +String state
        +String countryCode
        +String linkedinUrl
        +String githubUrl
        +String portfolioUrl
        +String summary
        +OffsetDateTime createdAt
        +OffsetDateTime updatedAt
    }

    class Organization {
        +UUID id
        +String name
        +String legalName
        +OrganizationType type
        +String description
        +OffsetDateTime createdAt
        +OffsetDateTime updatedAt
    }

    class Experience {
        +UUID id
        +UUID employerId
        +UUID clientId
        +String role
        +EmploymentType employmentType
        +String description
        +LocalDate startedAt
        +LocalDate endedAt
        +OffsetDateTime createdAt
        +OffsetDateTime updatedAt
    }

    class Project {
        +UUID id
        +UUID organizationId
        +String name
        +String description
        +OffsetDateTime createdAt
        +OffsetDateTime updatedAt
    }

    class Participation {
        +UUID id
        +UUID experienceId
        +UUID projectId
        +String description
        +OffsetDateTime createdAt
        +OffsetDateTime updatedAt
    }

    class Claim {
        +UUID id
        +UUID participationId
        +String content
        +OffsetDateTime createdAt
        +OffsetDateTime updatedAt
    }

    class Skill {
        +UUID id
        +String name
        +SkillCategory category
        +String[] aliases
        +OffsetDateTime createdAt
        +OffsetDateTime updatedAt
    }

    class ClaimSkill {
        +UUID claimId
        +UUID skillId
    }

    class Education {
        +UUID id
        +String institution
        +EducationType type
        +String title
        +String degree
        +String description
        +Integer workloadHours
        +LocalDate startedAt
        +LocalDate completedAt
        +Boolean expected
        +OffsetDateTime createdAt
        +OffsetDateTime updatedAt
    }

    class Language {
        +UUID id
        +String name
        +String proficiency
        +String description
        +OffsetDateTime createdAt
        +OffsetDateTime updatedAt
    }

    class VectorDocument {
        <<SpringAI>>
        +UUID id
        +String content
        +Map metadata
        +Vector embedding
    }

    class KnowledgeChunk {
        <<JPA>>
        +UUID id
        +UUID participationId
        +UUID claimId
        +UUID educationId
        +String hash
        +String model
        +OffsetDateTime indexedAt
    }

    class KnowledgeVectorStore {
        <<Service>>
        +add(KnowledgeDocument)
        +search(String)
        +delete(UUID)
    }

    class PgVectorStore {
        <<SpringAIAdapter>>
        +add(List~Document~)
        +similaritySearch(SearchRequest)
        +delete(List~String~)
    }

    class KnowledgeChunkRepository {
        <<Repository>>
        +save(KnowledgeChunk)
        +findById(UUID)
        +findByIds(List~UUID~)
        +deleteById(UUID)
    }

    class OrganizationType {
        <<enumeration>>
        COMPANY
        PUBLIC_INSTITUTION
        EDUCATION
        OTHER
    }

    class EmploymentType {
        <<enumeration>>
        EMPLOYEE
        INTERNSHIP
        CONTRACTOR
        OTHER
    }

    class SkillCategory {
        <<enumeration>>
        LANGUAGE
        FRAMEWORK
        DATABASE
        TOOL
        CONCEPT
        OTHER
    }

    class EducationType {
        <<enumeration>>
        DEGREE
        COURSE
        CERTIFICATION
    }

    Organization "1" <-- "0..*" Experience : employer
    Organization "0..1" <-- "0..*" Experience : client
    Organization "0..1" <-- "0..*" Project : context

    Experience "0..1" <-- "0..*" Participation : experience
    Project "0..1" <-- "0..*" Participation : project
    Participation "0..1" <-- "0..*" Claim : context

    Claim "1" <-- "0..*" ClaimSkill : claim
    Skill "1" <-- "0..*" ClaimSkill : skill

    Participation "0..1" <-- "0..*" KnowledgeChunk : source
    Claim "0..1" <-- "0..*" KnowledgeChunk : source
    Education "0..1" <-- "0..*" KnowledgeChunk : source
    VectorDocument "1" <-- "1" KnowledgeChunk : shared id PK FK

    KnowledgeVectorStore ..> PgVectorStore : delegates vectors
    KnowledgeVectorStore ..> KnowledgeChunkRepository : manages FKs
    PgVectorStore ..> VectorDocument : persists and searches
    KnowledgeChunkRepository ..> KnowledgeChunk : persists and loads

    note for Participation "At least one FK: experienceId or projectId. The pair must be unique, including NULLs."
    note for Claim "participationId is optional: standalone claims are allowed."
    note for KnowledgeChunk "Exactly one FK to a source. id is PK and FK to vector_store.id."
    note for VectorDocument "Spring AI schema: id, content, metadata, embedding."
    note for Profile "Single record per personal instance."

```

## Regras de domínio e persistência

1. **Instância pessoal:** `Profile` é um registro único por instalação. `Language` e `Education` pertencem implicitamente ao mesmo perfil; não é necessária uma tabela de usuários nem FKs para `Profile` no MVP.
2. **Organização e vínculo:** `Experience.employerId` é obrigatório. `Experience.clientId` é opcional; identifica o destinatário do serviço, que pode diferir do empregador (ex.: Qintess → TJPA). `Project.organizationId` é opcional e indica o contexto do projeto, não necessariamente seu proprietário legal.
3. **Participação flexível:** `Participation.experienceId` e `Participation.projectId` são individualmente opcionais, mas **ao menos um** precisa estar preenchido. Ela permite tanto atuação em um projeto durante um vínculo quanto atividades gerais de um vínculo e projetos pessoais.
4. **Participações não duplicadas:** criar `UNIQUE NULLS NOT DISTINCT (experience_id, project_id)` e `CHECK (num_nonnulls(experience_id, project_id) >= 1)` (PostgreSQL 15+).
5. **Afirmação autônoma:** `Claim.participationId` é **opcional**. Uma Claim pode existir sem estar vinculada a uma participação. `Claim` não tem `status` ou `sourceUrl`, conforme a decisão atual.
6. **Competências:** `ClaimSkill` representa N:N entre `Claim` e `Skill`, com PK composta (`claim_id`, `skill_id`). Não é indexado separadamente no pgvector; os nomes das skills enriquecem o texto derivado da Claim.
7. **Educação:** `Education.institution` é texto, sem `organization_id`; `expected = true` indica que `completedAt` é uma data **prevista**, e `false`, uma data efetiva (ou desconhecida se nula). Certificados não têm URL persistida nesta versão.
8. **Dois registros de indexação:** `VectorDocument` mapeia a tabela `vector_store` esperada pelo Spring AI (`id`, `content`, `metadata`, `embedding`). `KnowledgeChunk` mapeia outra tabela física, `knowledge_chunk`, com `id`, `participation_id`, `claim_id`, `education_id`, `hash`, `model`, `indexed_at`; ele **não** duplica `content` nem `embedding`.
9. **Associação 1:1 com PK compartilhada:** `knowledge_chunk.id` é `PRIMARY KEY` e `FOREIGN KEY REFERENCES vector_store(id)`. A mesma UUID identifica o `Document` vetorial e seu contexto relacional. O `KnowledgeChunk` associa **exatamente uma** entidade de origem (`Participation`, `Claim` ou `Education`): `CHECK (num_nonnulls(participation_id, claim_id, education_id) = 1)`.
10. **Integridade:** as FKs de `knowledge_chunk` apontam para as tabelas canônicas, não para IDs serializados em JSON. Os dados de `metadata` do `vector_store` são metadados opcionais de busca e **não substituem** essas FKs físicas.
11. **Metadados de indexação:** `hash` identifica o texto contextualizado que foi embutido; `model` registra o modelo de embeddings; `indexedAt` marca a última indexação concluída. Alterações na fonte ou nos contextos relacionados exigem atualizar a projeção quando o conteúdo ou o modelo mudar.
12. **Dimensionalidade:** `vector_store.embedding` usa `pgvector VECTOR(n)` com dimensão conforme o modelo. A tabela `vector_store` é criada por Flyway com esquema compatível e o Spring AI usa `initialize-schema=false`.
13. **Composição em Java:** `KnowledgeVectorStore` **usa** `PgVectorStore` para `add`, `similaritySearch` e `delete`; o `KnowledgeChunkRepository` administra o contexto relacional. Não herda de `PgVectorStore`, não sobrescreve `doAdd` e não reimplementa o algoritmo vetorial.
14. **Consistência entre tabelas:** gravar o `Document` com ID determinístico/estável, depois associar o `KnowledgeChunk` com o mesmo ID, utilizando uma transação da aplicação **se o caminho de gravação compartilhar a mesma transação/DataSource**. Confirmar rollback e upsert em testes Testcontainers. Apagar ou atualizar ambos mantendo integridade (por exemplo `ON DELETE CASCADE` de `vector_store` para `knowledge_chunk`); exclusão da fonte exige limpeza do índice no serviço.
15. **Leitura semântica:** `PgVectorStore.similaritySearch` retorna documentos e IDs. A aplicação busca os `KnowledgeChunk` correspondentes em lote e percorre as FKs até `Claim`, `Participation`, `Experience`, `Project`, `Organization` e `Education`. Nenhum `JOIN` vetorial precisa ser implementado manualmente.
16. **Fonte da verdade:** as entidades profissionais continuam canônicas. Ambas as tabelas de indexação são derivadas e podem ser reconstruídas; a reconstrução deve ser retomável e idempotente em execução serverless.

## Notas de modelagem

- O diagrama descreve o **primeiro módulo**, não inclui busca de vagas nem geração de currículos.
- `Organization`, `Experience`, `Project`, `Participation`, `Claim`, `Skill`, `Education`, `Language` e `Profile` são dados de domínio; `ClaimSkill` é associativa; `VectorDocument`/`vector_store` e `KnowledgeChunk`/`knowledge_chunk` são projeções técnicas de busca com relação 1:1.
- Embora o Mermaid represente nulidade principalmente pelas cardinalidades, os campos opcionais estão explicitados nas regras acima; `KnowledgeChunk.id` é obrigatório e compartilhado com `vector_store.id`.
- Datas (`LocalDate`) não incluem fuso; timestamps (`OffsetDateTime`) correspondem a `TIMESTAMPTZ` no PostgreSQL.


## Persistência vetorial por composição

```mermaid
sequenceDiagram
    participant App as Serviço de aplicação
    participant Facade as KnowledgeVectorStore
    participant AI as PgVectorStore (Spring AI)
    participant Repo as KnowledgeChunkRepository
    participant PG as PostgreSQL (Neon)

    App->>Facade: add(contexto, texto, fonte, hash, model)
    Facade->>AI: add(Document com id estável)
    AI->>PG: INSERT ou UPDATE vector_store
    Facade->>Repo: save(KnowledgeChunk com mesmo id e FK)
    Repo->>PG: INSERT ou UPDATE knowledge_chunk
    Note over Facade,PG: Testar rollback conjunto numa transação compartilhada
    App->>Facade: search(texto)
    Facade->>AI: similaritySearch(texto)
    AI->>PG: similaridade por embedding
    PG-->>AI: Document IDs e conteúdos
    AI-->>Facade: Documents
    Facade->>Repo: findByIds(ids)
    Repo->>PG: JOINs para fontes relacionais
    PG-->>Facade: FKs e contextos completos
    Facade-->>App: resultados enriquecidos
```

**Nota:** operações Spring AI e JPA/JDBC devem usar o mesmo banco e gerenciador de transação para garantir atomicidade; não pressupor que a transação atravessa outros bancos ou APIs. A chamada de embeddings é externa à transação PostgreSQL, e tentativas podem ser repetidas sem criar IDs novos.
