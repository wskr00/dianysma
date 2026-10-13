# Checklist de auditoria de erros reais

Escopo: leitura estática arquivo por arquivo do estado atual do repositório. Registo somente defeitos concretos que afetam o comportamento previsto; não proponho mudanças de código nesta auditoria. A ordem segue dependências, da fundação às tools e à configuração.

A marca `[x]` significa que o arquivo foi lido integralmente. Arquivos ainda não lidos ficam `[ ]`.

## Correções aplicadas depois desta auditoria

- [x] A configuração interna de JWT e os scopes por ferramenta foram removidos. O AgentCore Runtime V2 aplica o scope único `knowledge.access`; os achados de autorização interna abaixo foram substituídos por essa arquitetura.
- [x] Os campos opcionais de `ProfileDetails`, `ClaimDetails`, `EducationDetails`, `ExperienceDetails`, `ParticipationDetails` e `ProjectDetails` agora são declarados como nullable no schema MCP de saída.

## Problemas reais confirmados até agora

- Schemas MCP de saída incompatíveis com respostas reais: as tools usam `generateOutputSchema = true`, mas alguns DTOs retornam `null` em componentes declarados como tipos não anuláveis. O caso `ProfileDetails` foi observado no Inspector após a criação persistir. O mesmo defeito está confirmado nos contratos dos outros DTOs abaixo:
  - `ProfileDetails`: campos do perfil são opcionais; `get_profile`, `create_profile` e `update_profile` podem retornar `null`. Em `create_profile`, a persistência termina antes de o cliente rejeitar a resposta; por isso a tool informa falha apesar de criar o perfil.
  - `ClaimDetails`: `participationId` é nulo para claim autônoma (`get_claim`, `create_claim`, `update_claim`).
  - `EducationDetails`: grau, descrição, carga horária, datas e `expected` são opcionais (`get_education`, `list_education`, create/update).
  - `ExperienceDetails`: cliente, descrição e datas são opcionais (`get_experience`, `list_experiences`, create/update).
  - `ParticipationDetails`: `experienceId` ou `projectId` é necessariamente nulo, pois ao menos um dos dois é exigido; descrição também é opcional.
  - `ProjectDetails`: organização e descrição são opcionais.
- `docs/career-knowledge-class-diagram.md` e `docs/career-knowledge-class-diagram.mmd`: o diagrama não corresponde mais à implementação. Atribui ao `KnowledgeVectorStore` o uso de `KnowledgeChunkRepository` e operações de indexação/consulta por IDs que hoje pertencem a `KnowledgeVectorIndexer`; também documenta métodos de repositório que não existem.

## Leitura por dependência

### 1. Fundação e core

- [x] `src/main/java/io/github/wskr00/dianysma/DianysmaApplication.java` — Sem erro real identificado.
- [x] `src/main/java/io/github/wskr00/dianysma/core/audit/Auditable.java` — Sem erro real identificado.
- [x] `src/main/java/io/github/wskr00/dianysma/core/audit/mcp/McpHttpLoggingConfiguration.java` — Sem erro real identificado.
- [x] `src/main/java/io/github/wskr00/dianysma/core/audit/mcp/McpHttpLoggingFilter.java` — Sem erro real identificado.
- [x] `src/main/java/io/github/wskr00/dianysma/core/audit/mcp/McpToolAuditLogger.java` — Sem erro real identificado.

### 2. Enums do domínio

- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/domain/enums/EducationType.java` — Sem erro real identificado.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/domain/enums/LanguageProficiency.java` — Sem erro real identificado.

### 3. Entidades do domínio

- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/domain/Claim.java` — Sem erro real identificado.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/domain/Education.java` — Sem erro real identificado.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/domain/Experience.java` — Sem erro real identificado.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/domain/KnowledgeChunk.java` — Sem erro real identificado.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/domain/Language.java` — Sem erro real identificado.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/domain/Organization.java` — Sem erro real identificado.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/domain/Participation.java` — Sem erro real identificado.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/domain/Profile.java` — Sem erro real identificado.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/domain/Project.java` — Sem erro real identificado.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/domain/Skill.java` — Sem erro real identificado.

### 4. Repositórios

- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/repository/ClaimRepository.java` — Sem erro real identificado.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/repository/EducationRepository.java` — Sem erro real identificado.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/repository/ExperienceRepository.java` — Sem erro real identificado.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/repository/KnowledgeChunkRepository.java` — Sem erro real identificado.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/repository/LanguageRepository.java` — Sem erro real identificado.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/repository/OrganizationRepository.java` — Sem erro real identificado.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/repository/ParticipationRepository.java` — Sem erro real identificado.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/repository/ProfileRepository.java` — Sem erro real identificado.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/repository/ProjectRepository.java` — Sem erro real identificado.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/repository/SkillRepository.java` — Sem erro real identificado.

### 5. DTOs de entrada

- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/controller/dto/command/claim/CreateClaimCommand.java` — Sem erro real identificado.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/controller/dto/command/claim/ListClaimsCommand.java` — Sem erro real identificado.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/controller/dto/command/claim/UpdateClaimCommand.java` — Sem erro real identificado.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/controller/dto/command/common/ListKnowledgeRecordsCommand.java` — Sem erro real identificado.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/controller/dto/command/education/EducationCommand.java` — Sem erro real identificado.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/controller/dto/command/experience/ExperienceCommand.java` — Sem erro real identificado.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/controller/dto/command/knowledge/SearchKnowledgeCommand.java` — Sem erro real identificado.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/controller/dto/command/language/LanguageCommand.java` — Sem erro real identificado.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/controller/dto/command/organization/CreateOrganizationCommand.java` — Sem erro real identificado.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/controller/dto/command/organization/SearchOrganizationsCommand.java` — Sem erro real identificado.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/controller/dto/command/organization/UpdateOrganizationCommand.java` — Sem erro real identificado.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/controller/dto/command/participation/ParticipationCommand.java` — Sem erro real identificado.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/controller/dto/command/profile/ProfileCommand.java` — Sem erro real identificado.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/controller/dto/command/project/ProjectCommand.java` — Sem erro real identificado.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/controller/dto/command/skill/CreateSkillCommand.java` — Sem erro real identificado.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/controller/dto/command/skill/SearchSkillsCommand.java` — Sem erro real identificado.

### 6. DTOs de saída

- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/controller/dto/result/claim/ClaimDetails.java` — BUG REAL: `participationId` pode ser null, mas o DTO não o declara anulável; `ClaimTools` gera output schema e quebra para claims autônomas.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/controller/dto/result/claim/ClaimList.java` — Sem erro real identificado.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/controller/dto/result/common/DeleteResult.java` — Sem erro real identificado.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/controller/dto/result/education/EducationDetails.java` — BUG REAL: campos opcionais (`degree`, `description`, `workloadHours`, datas e `expected`) podem ser null, mas o schema gerado não permite null.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/controller/dto/result/education/EducationList.java` — Sem erro real identificado.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/controller/dto/result/experience/ExperienceDetails.java` — BUG REAL: `clientId`, `description` e datas podem ser null, mas o schema gerado não permite null.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/controller/dto/result/experience/ExperienceList.java` — Sem erro real identificado.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/controller/dto/result/knowledge/KnowledgeSearchHit.java` — Sem erro real identificado.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/controller/dto/result/knowledge/KnowledgeSearchResult.java` — Sem erro real identificado.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/controller/dto/result/language/LanguageDetails.java` — Sem erro real identificado.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/controller/dto/result/language/LanguageList.java` — Sem erro real identificado.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/controller/dto/result/organization/OrganizationDetails.java` — Sem erro real identificado.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/controller/dto/result/organization/OrganizationSearchResult.java` — Sem erro real identificado.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/controller/dto/result/participation/ParticipationDetails.java` — BUG REAL: uma associação (`experienceId` ou `projectId`) sempre será null pela regra de domínio; descrição também pode ser null, mas o schema não permite null.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/controller/dto/result/participation/ParticipationList.java` — Sem erro real identificado.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/controller/dto/result/profile/ProfileDetails.java` — BUG REAL observado: campos opcionais são retornados null, mas o output schema os declara como não anuláveis; Inspector rejeitou resposta de `get_profile` após a gravação persistir.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/controller/dto/result/project/ProjectDetails.java` — BUG REAL: organização e descrição são opcionais e podem ser null, mas o schema gerado não permite null.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/controller/dto/result/project/ProjectList.java` — Sem erro real identificado.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/controller/dto/result/skill/SkillDetails.java` — Sem erro real identificado.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/controller/dto/result/skill/SkillSearchResult.java` — Sem erro real identificado.

### 7. Exceções de aplicação

- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/service/exception/InvalidKnowledgeInputException.java` — Sem erro real identificado.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/service/exception/KnowledgeNotFoundException.java` — Sem erro real identificado.

### 8. Mapper e infraestrutura vetorial

- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/service/SkillDetailsMapper.java` — Sem erro real identificado.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/service/vector/KnowledgeVectorIndexer.java` — Sem erro real identificado; a FK de `knowledge_chunk` para `vector_store` em cascata remove a projeção ao apagar o vetor.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/service/vector/KnowledgeVectorReindexRunner.java` — Sem erro real identificado.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/service/vector/KnowledgeVectorStore.java` — Sem erro real identificado.

### 9. Serviços de aplicação

- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/service/ClaimService.java` — Sem verificação de autenticação/autorização local; exclusão do vetor aguardando cruzamento da FK.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/service/EducationService.java` — Sem verificação de autenticação/autorização local; DTO de saída nullable também quebra o output schema.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/service/ExperienceService.java` — Sem verificação de autenticação/autorização local; DTO de saída tem campos nulos a confirmar com a tool.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/service/KnowledgeSearchService.java` — Sem verificação de autenticação/autorização local.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/service/LanguageService.java` — Sem verificação de autenticação/autorização local.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/service/OrganizationService.java` — Sem verificação de autenticação/autorização local.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/service/ParticipationService.java` — Sem verificação de autenticação/autorização local; DTO de saída nullable também quebra o output schema.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/service/ProfileService.java` — BUG REAL no retorno de `get_profile`/`create_profile` está em `ProfileDetails` (schema de saída); a migração tem índice singleton que protege corridas de criação.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/service/ProjectService.java` — Sem verificação de autenticação/autorização local; exclusão/relacionamentos aguardam cruzamento da FK; DTO de saída tem campos nulos a confirmar com a tool.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/service/SkillService.java` — Sem verificação de autenticação/autorização local; a migração tem índice único case-insensitive que protege a corrida de criação.

### 10. Ferramentas MCP

- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/controller/mcp/ClaimTools.java` — BUG REAL: retorno contém campos null que contradizem o output schema; ver achado sistêmico confirmado acima.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/controller/mcp/EducationTools.java` — BUG REAL: retorno contém campos null que contradizem o output schema; ver achado sistêmico confirmado acima.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/controller/mcp/ExperienceTools.java` — BUG REAL: retorno contém campos null que contradizem o output schema; ver achado sistêmico confirmado acima.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/controller/mcp/KnowledgeSearchTools.java` — Sem outro erro real identificado; tools geram output schema para seus tipos de saída.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/controller/mcp/LanguageTools.java` — Sem outro erro real identificado; tools geram output schema para seus tipos de saída.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/controller/mcp/McpToolErrors.java` — Sem erro real identificado na tradução/log dos erros do serviço.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/controller/mcp/OrganizationTools.java` — Sem outro erro real identificado; tools geram output schema para seus tipos de saída.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/controller/mcp/ParticipationTools.java` — BUG REAL: retorno contém campos null que contradizem o output schema; ver achado sistêmico confirmado acima.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/controller/mcp/ProfileTools.java` — BUG REAL: retorno contém campos null que contradizem o output schema; ver achado sistêmico confirmado acima.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/controller/mcp/ProjectTools.java` — BUG REAL: retorno contém campos null que contradizem o output schema; ver achado sistêmico confirmado acima.
- [x] `src/main/java/io/github/wskr00/dianysma/knowledge/controller/mcp/SkillTools.java` — Sem outro erro real identificado; tools geram output schema para seus tipos de saída.

### 11. Recursos da aplicação e migração

- [x] `src/main/resources/application.yml` — Configuração de OAuth/JWT removida; issuer, audience e scope são validados pelo AgentCore fora do processo Java.
- [x] `src/main/resources/db/migration/V1__initial_schema.sql` — Sem erro real de integridade identificado; índices únicos e FKs confirmam as proteções citadas nos serviços, incluindo a cascata da projeção vetorial.

### 12. Testes existentes (leitura estática)

- [x] `src/test/java/io/github/wskr00/dianysma/DianysmaApplicationTests.java` — Apenas teste de contexto; não cobre schema de saída MCP, nulabilidade dos DTOs nem autorização por scope.
- [x] `src/test/java/io/github/wskr00/dianysma/TestDianysmaApplication.java` — Aplicação de teste do Spring Boot; sem erro real identificado.
- [x] `src/test/java/io/github/wskr00/dianysma/TestcontainersConfiguration.java` — Configura PostgreSQL com pgvector para teste; sem erro real identificado.

### 13. Build, execução e container

- [x] `.env.example` — Sem erro real identificado; contém as variáveis esperadas sem valores secretos.
- [x] `.gitignore` — Sem erro real identificado para o checkout auditado.
- [x] `.gitattributes` — Sem erro real identificado; define finais de linha e binários.
- [x] `build.gradle` — Sem erro real identificado na configuração de dependências/build para o escopo atual.
- [x] `compose.yaml` — Sem erro real identificado na configuração do PostgreSQL local com pgvector.
- [x] `docker/postgres/001-extensions.sql` — Sem erro real identificado; inicializa extensões utilizadas pelo banco local.
- [x] `gradle/wrapper/gradle-wrapper.jar` — Inspecionado como binário pelo tipo e checksum; nenhum problema concreto identificado.
- [x] `gradle/wrapper/gradle-wrapper.properties` — Sem erro real identificado.
- [x] `gradlew` — Script wrapper POSIX lido; sem erro real identificado.
- [x] `gradlew.bat` — Script wrapper Windows lido; sem erro real identificado.
- [x] `settings.gradle` — Sem erro real identificado.
- [x] `skill/.gitkeep` — Arquivo vazio usado como placeholder; sem erro real identificado.

### 14. Documentação e diagramas

- [x] `docs/architecture-components.mmd` — Sem erro real identificado no diagrama de componentes.
- [x] `docs/architecture-overview.md` — A arquitetura Cognito/AgentCore foi atualizada para o scope único e validação JWT externa ao processo Java.
- [x] `docs/career-knowledge-class-diagram.md` — BUG REAL de documentação: responsabilidades e métodos do subsistema vetorial estão desatualizados; ver achado acima.
- [x] `docs/career-knowledge-class-diagram.mmd` — BUG REAL de documentação: replica o diagrama desatualizado do `.md`, inclusive as responsabilidades do `KnowledgeVectorStore` e métodos inexistentes do repositório.
- [x] `docs/mcp-tool-contract.md` — BUG REAL de documentação: não registra que os DTOs opcionais retornam `null` enquanto o output schema gerado declara campos como não anuláveis; ver achado de schema MCP.
- [x] `docs/code-review-checklist.md` — Checklist desta auditoria; lida e atualizada durante a revisão.

## Progresso

Auditoria estática concluída para os arquivos versionados e visíveis no checkout, em ordem de dependências. `.env` local foi excluído por conter configuração/segredos de ambiente e não fazer parte do código versionado. Nenhum teste foi executado; os testes existentes foram apenas lidos. Não houve alteração em código de implementação.
