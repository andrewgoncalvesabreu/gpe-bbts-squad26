# Guia para agentes - SIGES-PES / GPE (BBTS)

## Propósito do projeto

Este repositório contém o **Sistema de Gestão de Parcerias Estratégicas (SIGES-PES/GPE)** da BB Tecnologia e Serviços (BBTS). O sistema apoia o ciclo de vida das parcerias institucionais: identificação de oportunidades, avaliação, aprovação, formalização, acompanhamento e governança.

Use o `README.md` como referência de execução local e arquitetura geral. Antes de afirmar que um requisito está concluído, confira o código, as migrations e as rotas atuais: parte dos requisitos está em desenvolvimento.

## Requisitos de negócio

O escopo de referência abrange os seguintes módulos:

- Parceiros: cadastro, classificação e histórico.
- Oportunidades: registro, avaliação por critérios e fluxo de aprovação.
- Instrumentos jurídicos: cadastro, vigência, renovações e alertas.
- Gestão documental, projetos, indicadores, governança, riscos, financeiro, relatórios e auditoria.

Os requisitos não funcionais incluem autenticação corporativa/SSO, RBAC, trilha de auditoria, TLS, API REST/JSON, interface responsiva e acessível (WCAG 2.1 AA), execução em contêineres e possibilidade de escalabilidade horizontal.

Não invente regras de negócio quando o requisito não detalhar seu comportamento. Registre a dúvida e peça confirmação ao usuário.

## Arquitetura e estrutura

- `gpe-backend/`: API REST em Java 21 e Spring Boot, com Spring Data JPA, Spring Security, Bean Validation e Flyway.
- `gpe-backend/src/main/resources/db/migration/`: versionamento do esquema PostgreSQL.
- `gpe-frontend/`: React, TypeScript, Vite, React Router, PrimeReact, Tailwind CSS e Axios.
- `gpe-frontend/src/services/`: acesso à API; mantenha as chamadas HTTP centralizadas aqui.
- `gpe-frontend/src/types/`: tipos compartilhados do domínio.
- `gpe-frontend/src/pages/`: telas por módulo; `components/` contém elementos reutilizáveis e layout.
- `docker-compose.yml`: PostgreSQL 16 e Keycloak para desenvolvimento local.

O back-end expõe a API sob `/api/v1`. Em desenvolvimento, o front-end usa o proxy do Vite para esse prefixo. Preserve esse contrato ao criar ou modificar endpoints.

## Estado conhecido

Há implementação para os módulos de parceiros e oportunidades, incluindo rotas e endpoints de oportunidades para avaliação e alteração de status. Isso não significa que todos os requisitos do sistema estejam cobertos.

O repositório pode conter alterações locais e arquivos não rastreados que não pertencem à tarefa atual. Preserve-os: não reverta, descarte, mova ou formate arquivos não relacionados sem autorização explícita.

## Convenções de implementação

- Use código e nomes técnicos consistentes com o padrão já presente em cada camada.
- Mantenha a separação controller -> service -> repository no back-end.
- Valide entradas HTTP com Bean Validation e mantenha o tratamento de erros centralizado.
- No front-end, atualize tipos, serviços e páginas de maneira coerente quando um contrato de API mudar.
- Prefira componentes reutilizáveis para elementos de interface repetidos.
- Não exponha senhas, tokens, chaves ou dados sensíveis em código, logs ou documentação.

## Banco de dados e migrations

- O Hibernate está configurado para validar o esquema; a evolução estrutural pertence ao Flyway.
- Para qualquer mudança de esquema, crie uma nova migration versionada em `gpe-backend/src/main/resources/db/migration/`.
- Nunca altere uma migration que possa já ter sido executada em ambiente compartilhado.
- Avalie impacto em entidades, repositórios, DTOs, serviços, endpoints e front-end antes de modificar o modelo de dados.

## Verificação

Execute somente as verificações pertinentes à alteração e informe o resultado observado.

- Back-end: `cd gpe-backend; .\\mvnw.cmd test`
- Front-end: `cd gpe-frontend; npm run lint`
- Build do front-end: `cd gpe-frontend; npm run build`

Para executar localmente, inicie o banco com `docker-compose up -d postgres-gpe`; a API usa a porta `8081` e o Vite normalmente usa a `5173`.

## Controle de versão e limites de atuação

- Não faça commit, push, criação de pull request, deploy ou alteração de infraestrutura externa sem solicitação explícita.
- Ao criar branch, use o prefixo `codex/`, salvo instrução diferente do usuário.
- Use mensagens de commit no padrão Conventional Commits quando um commit for solicitado.
- Em tarefas que envolverem novos módulos ou interpretação relevante dos requisitos, apresente o plano e as premissas antes de editar o código.
