# SIGES-PES / GPE — arquitetura e funcionamento

Este documento descreve como o código do Sistema de Gestão de Parcerias Estratégicas (GPE) está organizado, como as tecnologias colaboram e quais fluxos de negócio estão implementados. Ele complementa o README e deve ser atualizado quando a arquitetura ou os contratos da API mudarem.

## 1. Visão geral

O repositório é um monorepo com três partes:

| Diretório | Responsabilidade |
| --- | --- |
| `gpe-frontend/` | Aplicação web de página única (SPA), construída com React e TypeScript. |
| `gpe-backend/` | API REST Java/Spring, regras de negócio, integração com o banco e armazenamento de documentos. |
| `docker-compose.yml` | Ambiente local com PostgreSQL e Keycloak em modo de desenvolvimento. |

O fluxo normal de uma operação é:

1. A pessoa usuária interage com uma página React.
2. Um serviço do frontend envia uma requisição HTTP à API usando Axios.
3. Um controller Spring interpreta a rota, os parâmetros e o corpo da requisição.
4. O service aplica validações e regras de negócio; quando necessário, consulta outros services.
5. Repositories do Spring Data JPA leem e gravam entidades no PostgreSQL.
6. A API converte o resultado em JSON (ou em arquivo para downloads) e o frontend atualiza a tela.

O prefixo padrão da API é `/api/v1`. Em desenvolvimento, o servidor Vite encaminha `/api` para `http://localhost:8081`, evitando configurar CORS para esse fluxo local.

## 2. Tecnologias e o papel de cada uma

### Frontend

- **React 19** organiza a interface em componentes e páginas; os estados locais controlam formulários, carregamento, listas e mensagens.
- **TypeScript 6** verifica tipos de props, dados e respostas da API durante a compilação.
- **React Router** associa URLs da aplicação às páginas de parceiros, instrumentos, oportunidades e projetos.
- **Axios** centraliza chamadas HTTP, a URL-base e o tempo limite; `FormData` é utilizado nos uploads e `Blob` nos downloads.
- **PrimeReact** fornece controles de interface, como botões, entradas, tabelas e mensagens.
- **Tailwind CSS** fornece classes utilitárias para o layout e a apresentação visual.
- **Vite** executa o servidor local, aplica o proxy da API e gera os arquivos estáticos de produção. `npm run build` executa primeiro `tsc -b` e depois a compilação Vite.
- **Oxlint** verifica problemas de lint pelo comando `npm run lint`.

### Backend

- **Java 21** é a plataforma de execução e compilação.
- **Spring Boot 4.1.1** inicializa a aplicação, configura os componentes e publica a API HTTP na porta 8081.
- **Spring MVC** mapeia rotas REST para controllers e trata JSON, multipart e downloads.
- **Spring Data JPA / Hibernate** mapeiam entidades Java para tabelas e implementam consultas a partir das interfaces de repository. Hibernate está configurado em `ddl-auto: validate`: confere o schema, mas não cria nem altera tabelas.
- **Jakarta Bean Validation** valida os DTOs e corpos de requisição anotados com `@Valid`.
- **Spring Scheduling** habilita a tarefa periódica de alertas de vencimento.
- **Lombok** reduz código repetitivo nos modelos (construtores, getters/setters e builders).
- **Maven** resolve dependências, compila e executa os testes.
- **Flyway** aplica migrações SQL versionadas antes de a aplicação usar o schema.
- **PostgreSQL 16** guarda parceiros, oportunidades, instrumentos, históricos, documentos e projetos.
- **Spring Security** está incluído, mas a configuração atual permite acesso a todas as rotas e desabilita CSRF. Portanto, ainda não existe autenticação nem autorização efetiva na API.

### Infraestrutura e armazenamento

- **Docker Compose** sobe PostgreSQL e Keycloak para desenvolvimento local. O Keycloak está em `start-dev`; sua presença no Compose, por si só, não integra autenticação com o backend.
- Os metadados, relações e históricos dos documentos ficam no PostgreSQL. Os bytes dos arquivos ficam em um diretório de filesystem definido por `DOCUMENT_STORAGE_PATH`, fora dos recursos estáticos públicos da aplicação.
- O filesystem de documentos precisa ser persistente e entrar no plano de backup junto com o banco. O volume `pgdata` do Compose preserva somente os dados do PostgreSQL, não os arquivos enviados.

## 3. Organização do código

No backend, o pacote principal é `br.com.bbts.gpe.gpe_backend`:

| Pacote | Papel |
| --- | --- |
| `controller/` | Endpoints HTTP, leitura de parâmetros e construção de respostas. |
| `service/` | Regras de negócio, validações, transações e coordenação de repositories/provedores. |
| `domain/model/` | Entidades JPA que representam os dados persistidos. |
| `domain/repository/` | Interfaces Spring Data JPA para consultas e persistência. |
| `dto/` | Formatos de entrada e saída da API. |
| `exception/` | Exceções de negócio, recurso não encontrado, arquivo e integração. |
| `config/` | Configurações transversais, incluindo a configuração atual de segurança. |

O controller deve ser a fronteira HTTP; regras que alteram o estado do domínio ficam nos services. Os repositories abstraem consultas ao banco e as entidades definem os relacionamentos persistidos.

No frontend, `src/pages/` contém as páginas dos módulos; `src/components/` contém componentes reutilizáveis; `src/services/` encapsula as chamadas à API; `src/types/` descreve os contratos TypeScript; `src/routes/` define a navegação; e `src/utils/` contém utilitários compartilhados, como tratamento de erros e formatação de datas.

## 4. Módulos e fluxos de negócio

### 4.1 Parceiros e classificação (US001–US002)

O cadastro permite criar, consultar, atualizar e inativar parceiros. Os dados incluem CNPJ, razão social, nome fantasia, tipo de organização e demais informações cadastrais. A aplicação valida os campos e o CNPJ, rejeita duplicidade e associa a classificação organizacional ao parceiro.

A inativação altera o status para `INATIVO` em vez de apagar o registro. Operações relevantes recebem a matrícula do responsável no cabeçalho `X-Responsavel-Matricula`; a matrícula é armazenada como informação declarada, não autenticada.

### 4.2 Histórico do parceiro (US003)

O histórico é ordenado do evento mais recente para o mais antigo. O sistema registra eventos ligados a alterações cadastrais e a operações como oportunidades, avaliações, decisões do fluxo, instrumentos, documentos e assinatura. A API também expõe operações para consultar e registrar eventos de histórico.

O evento guarda categoria, descrição, data/hora e matrícula informada. O histórico é um registro de eventos da aplicação; não representa trilha inviolável nem identidade comprovada.

### 4.3 Oportunidades, avaliação e aprovação (US004–US006)

Uma oportunidade é associada a um parceiro existente e reúne título, origem, descrição, objetivos, justificativa, benefícios, problema/solução, estimativa financeira, área demandante e responsável.

O fluxo usa estados como `TRIAGEM`, `EM_AVALIACAO`, `EM_APROVACAO` e `CANCELADA`. O registro de avaliação contém alinhamento estratégico, potencial de inovação, retorno financeiro, impacto institucional e viabilidade técnica, além de parecer e matrícula do avaliador. A primeira avaliação move a oportunidade de triagem para avaliação; o envio à aprovação exige ao menos uma avaliação. Aprovação e reprovação registram a decisão no histórico, e a reprovação recebe justificativa.

O código não implementa autorização de aprovador: a matrícula recebida na requisição não comprova que a pessoa possui o perfil necessário.

### 4.4 Instrumentos, vigência e alertas (US007–US009)

O instrumento jurídico é associado a um parceiro e pode apontar para uma oportunidade do mesmo parceiro. A classificação contempla Convênio, Acordo de Cooperação, Memorando de Entendimento, Contrato e Termo de Parceria. A vigência exige datas inicial e final, e a data final não pode anteceder a inicial.

Uma prorrogação precisa estender a data final vigente; o sistema salva a data anterior, a nova data e a justificativa, além de atualizar a vigência atual. Os alertas identificam instrumentos com vencimento entre hoje e os próximos 30 dias, inclusive; instrumentos já vencidos ficam fora do resultado. A API permite consultar os alertas, a interface os apresenta e a tarefa agendada registra os alertas no log diariamente às 08:00 (horário da JVM), por padrão. Não há integração com serviço externo de notificação.

### 4.5 Documentos, versões e assinatura (US010–US012)

Um documento é associado a um parceiro e a uma parceria formalizada daquele parceiro. O upload recebe o arquivo e a matrícula do responsável via `multipart/form-data`. O backend aceita PDF, DOCX, XLSX, PNG, JPG e JPEG, com limite de 20 MB; confere extensão e assinatura básica do conteúdo, rejeita pacotes Office que contenham macro VBA e calcula o hash SHA-256. A validação não substitui antivírus nem análise completa de segurança do conteúdo.

O arquivo recebe chave aleatória e é gravado em `DOCUMENT_STORAGE_PATH`. A tabela de documento guarda seus metadados e versão atual; cada versão conserva nome, tipo MIME, tamanho, hash, matrícula e data do upload. Criar uma versão adiciona um novo registro e não sobrescreve as anteriores. A API oferece download da versão atual e das versões antigas.

O envio para assinatura usa a interface `DocumentSignatureProvider`. A implementação atual é um mock local que gera um protocolo e um status configurável; não conecta a um provedor real e não produz assinatura eletrônica juridicamente válida. O status, protocolo, detalhes e responsável ficam no histórico de assinatura. `SIGNATURE_MOCK_FAILURE=true` permite simular falha; `SIGNATURE_MOCK_RESULT_STATUS` define um dos estados aceitos pelo mock.

### 4.6 Parcerias, projetos e planos de trabalho (US013–US014)

Uma parceria formalizada liga um parceiro a um instrumento jurídico desse mesmo parceiro; um instrumento não pode ser vinculado a mais de uma parceria. A criação rejeita parceiro inativo.

O projeto exige uma parceria existente e contém nome, descrição e status. A operação de exclusão marca o projeto como `CANCELADO`, preservando o registro. Um projeto pode ter um plano de trabalho com objetivos, intervalo do cronograma, responsáveis, entregas e marcos. O serviço rejeita cronograma invertido e datas de entregas ou marcos fora do intervalo. Os itens do plano são persistidos em entidades relacionadas ao plano.

## 5. Persistência e evolução do schema

As migrações em `gpe-backend/src/main/resources/db/migration/` são executadas em ordem pelo Flyway. O conjunto atual vai de V1 a V11:

- V1–V7 criam e ampliam dados de parceiros, oportunidades, avaliações, workflow, instrumentos e vigência.
- V8 acrescenta os dados de responsável ao histórico.
- V9 cria documentos, versões e eventos de assinatura.
- V10 cria parcerias, projetos e planos de trabalho, incluindo objetivos, entregas e marcos.
- V11 associa documentos a parcerias formalizadas.

Flyway é a fonte de evolução do schema; Hibernate apenas valida se o mapeamento e o schema estão compatíveis. Não renomeie nem edite uma migração que já foi aplicada em ambientes compartilhados: crie uma nova versão.

## 6. API disponível

Todas as rotas abaixo são relativas ao prefixo `/api/v1`.

| Módulo | Rotas principais |
| --- | --- |
| Parceiros | `GET /parceiros`, `GET /parceiros/{id}`, `POST /parceiros`, `PUT /parceiros/{id}`, `DELETE /parceiros/{id}` (inativação). |
| Histórico | `GET /parceiros/{parceiroId}/historico`, `POST /parceiros/{parceiroId}/historico`. |
| Oportunidades | `GET /oportunidades`, `GET /oportunidades/{id}`, `POST /oportunidades`, `PUT /oportunidades/{id}`, `DELETE /oportunidades/{id}` (cancelamento), `POST/GET /oportunidades/{id}/avaliacoes`, `POST /oportunidades/{id}/enviar-aprovacao`, `POST /oportunidades/{id}/aprovar`, `POST /oportunidades/{id}/reprovar`. |
| Instrumentos | `GET /instrumentos`, `POST /instrumentos`, `GET /instrumentos/alertas/vencimento`, `GET/POST /instrumentos/{id}/prorrogacoes`. |
| Documentos | `POST /parceiros/{parceiroId}/documentos`, `GET /parceiros/{parceiroId}/documentos`, `GET /documentos/{documentoId}` (download atual). O multipart de upload requer `parceriaId`, `file` e `responsavelMatricula`; `nome` e `descricao` são opcionais na API. |
| Versões e assinatura | `POST /documentos/{documentoId}/versoes`, `GET /documentos/{documentoId}/versoes`, `GET /documentos/{documentoId}/versoes/{numeroVersao}/arquivo`, `POST/GET /documentos/{documentoId}/assinaturas`. |
| Parcerias | `GET /parcerias` (aceita filtro `parceiroId`), `POST /parcerias`. |
| Projetos | `GET /projetos` (aceita filtro `parceriaId`), `GET /projetos/{id}`, `POST /projetos`, `PUT /projetos/{id}`, `DELETE /projetos/{id}` (cancelamento). |
| Plano de trabalho | `GET/POST/PUT/DELETE /projetos/{projetoId}/plano-trabalho`. |

Erros de validação, regras de negócio, recurso inexistente, arquivo e integração são convertidos em respostas HTTP centralizadas pelo `GlobalExceptionHandler`. Em linhas gerais, as respostas distinguem `400` (dados/arquivo inválidos), `404` (recurso ausente), `409` (conflito de negócio/dados), `500` (armazenamento) e `502` (integração de assinatura).

## 7. Configuração e execução local

### Variáveis e valores relevantes

| Variável/configuração | Padrão | Uso |
| --- | --- | --- |
| `DOCUMENT_STORAGE_PATH` | `./data/documents` | Diretório dos bytes dos documentos. Deve ser persistente. |
| `SIGNATURE_MOCK_FAILURE` | `false` | Quando verdadeiro, simula indisponibilidade do mock. |
| `SIGNATURE_MOCK_RESULT_STATUS` | `AGUARDANDO_ASSINATURA` | Status simulado: `AGUARDANDO_ASSINATURA`, `ASSINADO`, `RECUSADO` ou `CANCELADO`. |
| `app.alertas.instrumentos.cron` | `0 0 8 * * *` | Expressão cron da tarefa de alertas. |
| `VITE_API_URL` | `/api/v1` | URL-base alternativa da API no frontend. |

As credenciais e senhas existentes no Compose e no YAML são valores de desenvolvimento. Não devem ser reutilizadas em produção.

### Comandos

Na raiz do repositório, inicie PostgreSQL:

```powershell
docker compose up -d postgres-gpe
```

Inicie o backend:

```powershell
cd gpe-backend
.\mvnw.cmd spring-boot:run
```

Em outro terminal, instale dependências e inicie o frontend:

```powershell
cd gpe-frontend
npm install
npm run dev
```

Validações usadas no projeto:

```powershell
# No diretório gpe-backend
.\mvnw.cmd clean test

# No diretório gpe-frontend
npm run build
npm run lint
```

## 8. Segurança, operação e limites atuais

1. **Autenticação e autorização:** apesar das dependências de segurança e do serviço Keycloak no Compose, a configuração da API usa `permitAll` e desativa CSRF. As rotas não estão protegidas por login ou papéis.
2. **Matrícula declarada:** campos e cabeçalhos de matrícula registram o valor enviado, sem confirmar identidade. Não usar isso como trilha auditável confiável ou controle de aprovadores.
3. **Assinatura:** o provedor é mock e não tem validade jurídica. Para produção, implementar e configurar um provedor real, validar callbacks/retornos e proteger segredos e protocolos.
4. **Arquivos:** o backend restringe formato, tamanho e algumas assinaturas de conteúdo, mas não executa antivírus. Em produção, usar armazenamento gerenciado e análise de arquivos adequada; manter backup consistente dos bytes e metadados.
5. **Alertas:** o frontend consulta os próximos 30 dias e o agendador escreve no log. Não há envio de e-mail, push ou integração com sistema de notificação.
6. **Observabilidade:** logs SQL estão em nível DEBUG no YAML atual; ajustar níveis, dados sensíveis e retenção antes de usar em um ambiente corporativo.
7. **Infraestrutura:** Keycloak usa `start-dev` e credenciais fixas no Compose; essa configuração é somente para desenvolvimento.

## 9. Verificação da implementação

No desenvolvimento documentado, a suíte limpa do backend executou 33 testes sem falhas e inicializou o contexto Spring com as migrações até V11. O build de produção do frontend também concluiu. O lint termina com avisos de regras React em alguns efeitos de carregamento e um aviso de fast refresh; esses avisos não impedem o build.
