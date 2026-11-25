# 🏛️ SIGES-PES / GPE

### Sistema de Gestão de Parcerias Estratégicas — BBTS

> **Projeto Squad 26 | 4º Período Takeoff**

O **SIGES-PES / GPE** é um sistema corporativo desenvolvido para a **BB Tecnologia e Serviços (BBTS)**, destinado ao **mapeamento, prospecção, avaliação e governança do ciclo de vida de parcerias estratégicas**.

O sistema busca apoiar a gestão das parcerias em conformidade com as **normas institucionais corporativas**, como a **NI035** e a **PRO035-001**.

---

## 📌 Sobre o Projeto

O GPE tem como objetivo centralizar e organizar informações relacionadas às parcerias estratégicas da BBTS, permitindo acompanhar todo o ciclo de vida das oportunidades.

Entre as principais funcionalidades previstas estão:

* 🔎 Mapeamento de parceiros e oportunidades
* 🤝 Gestão de parcerias estratégicas
* 📄 Cadastro e acompanhamento de instrumentos jurídicos vinculados a parceiros e oportunidades
* 📊 Avaliação e acompanhamento de oportunidades
* 📋 Gestão de informações e documentos
* 📈 Dashboards e indicadores gerenciais
* 🔐 Controle de acesso e autenticação
* 🏛️ Governança e conformidade com normas corporativas

## Funcionalidades implementadas

Para a explicação da arquitetura, dos fluxos completos e dos limites operacionais, consulte [Documentação de arquitetura e funcionamento](./docs/ARQUITETURA-E-FUNCIONAMENTO.md).

* Cadastro e classificação de parceiros com histórico de alterações e identificação do responsável pela matrícula informada.
* Registro e avaliação de oportunidades, com envio para aprovação, decisão e justificativa de reprovação.
* Cadastro de instrumentos, controle e prorrogação de vigência, histórico de eventos e alertas de vencimento na interface e no log agendado do backend.
* Os endpoints de cadastro/alteração/inativação de parceiros e cadastro de instrumentos recebem a matrícula no cabeçalho `X-Responsavel-Matricula`; avaliações e decisões de oportunidades recebem a matrícula no corpo da requisição.
* Os fluxos que coletam matrícula registram o valor informado pelo operador. Como não há autenticação configurada, a matrícula não comprova identidade e as decisões de aprovação ainda não têm controle de autorização por perfil.
* Documentos podem ser anexados a parcerias formalizadas, baixados e versionados; o sistema registra o histórico de solicitações de assinatura.
* Parcerias formalizadas vinculam um parceiro a um instrumento jurídico; projetos e planos de trabalho são vinculados a essas parcerias e projetos.

### Documentos, assinatura e projetos (US010–US014)

O upload aceita PDF, DOCX, XLSX, PNG e JPG/JPEG, com limite de 20 MB por arquivo. O back-end valida a extensão e a assinatura básica do conteúdo, rejeita arquivos Office com macros e calcula SHA-256. Os arquivos são guardados fora do diretório público da aplicação. Configure `DOCUMENT_STORAGE_PATH` para apontar ao armazenamento persistente; o padrão é `./data/documents`, relativo ao diretório de execução do back-end. Em contêiner, monte esse caminho como volume persistente. O banco guarda os metadados e o histórico, enquanto os bytes ficam nesse armazenamento, que deve ser preservado em backup junto com o banco.

A integração de assinatura é um mock local substituível por um provedor real. Ela recebe a versão do documento e produz um protocolo/status simulado, sem efetuar assinatura eletrônica com validade jurídica. Para testar os caminhos, `SIGNATURE_MOCK_FAILURE=true` simula falha e `SIGNATURE_MOCK_RESULT_STATUS` aceita `AGUARDANDO_ASSINATURA`, `ASSINADO`, `RECUSADO` ou `CANCELADO` (padrão `AGUARDANDO_ASSINATURA`). Use essas configurações apenas no ambiente apropriado; não configure o mock como integração de produção.

Principais endpoints adicionados (prefixo `/api/v1`):

| Recurso | Endpoints |
| --- | --- |
| Documentos (US010–US012) | `POST /parceiros/{parceiroId}/documentos` (requer `parceriaId` no multipart), `GET /parceiros/{parceiroId}/documentos`, `GET /documentos/{documentoId}` |
| Versões e assinatura | `POST /documentos/{documentoId}/versoes`, `GET /documentos/{documentoId}/versoes`, `GET /documentos/{documentoId}/versoes/{numeroVersao}/arquivo`, `POST/GET /documentos/{documentoId}/assinaturas` |
| Parcerias e projetos (US013) | `POST/GET /parcerias`, `POST/GET /projetos`, `GET/PUT/DELETE /projetos/{id}` |
| Plano de trabalho (US014) | `GET/POST/PUT/DELETE /projetos/{projetoId}/plano-trabalho` |

O cadastro de projeto exige uma parceria existente. Planos de trabalho suportam objetivos, entregas, cronograma, responsáveis e marcos; datas de entregas e marcos precisam estar dentro do período definido no plano.

---

# 🏗️ Arquitetura

O projeto utiliza uma arquitetura em **Monorepo**, mantendo Back-end, Front-end e Banco de Dados organizados dentro de um único repositório.

### Tecnologias utilizadas

| Camada                    | Tecnologia                 |
| ------------------------- | -------------------------- |
| ⚙️ Back-end               | Java 21 LTS                |
| 🚀 Framework              | Spring Boot 4.1.1          |
| 🗄️ Banco de Dados        | PostgreSQL 16              |
| 🔄 Migrações              | Flyway                     |
| 🔐 Segurança              | Spring Security + OAuth2 Resource Server + Keycloak (JWT/RBAC) |
| 🌐 Front-end              | React + TypeScript         |
| ⚡ Build Front-end         | Vite                       |
| 🎨 UI                     | PrimeReact + Tailwind CSS  |
| 🐳 Infraestrutura         | Docker + Docker Compose    |
| 📡 Comunicação            | API REST / HTTP            |
| 📦 Gerenciamento Back-end | Maven                      |

### Back-end

O Back-end foi desenvolvido utilizando **Java 21** e **Spring Boot 4.1.1**, contando com:

* Spring Data JPA
* Spring Security com validação de tokens JWT emitidos pelo Keycloak
* Perfis RBAC de desenvolvimento: `ADMIN`, `GESTOR_PARCEIRAS`, `ANALISTA`, `APROVADOR`, `AUDITOR` e `CONSULTA`
* Bean Validation
* Flyway
* Maven
* API REST

### Front-end

O Front-end utiliza:

* React
* TypeScript
* Vite
* PrimeReact
* Tailwind CSS
* Axios

### Banco de Dados

O projeto utiliza **PostgreSQL 16**.

As alterações na estrutura do banco são controladas pelo **Flyway**, permitindo o versionamento e a aplicação automática das migrações SQL.

### Infraestrutura

O ambiente local utiliza **Docker e Docker Compose** para facilitar a configuração e execução dos serviços necessários ao projeto.

O Keycloak importa o realm de desenvolvimento localizado em `infra/keycloak/gpe-realm.json`. As credenciais abaixo são exclusivamente locais:

| Usuário | Senha | Perfis principais |
| --- | --- | --- |
| `admin` | `admin` | ADMIN, GESTOR_PARCEIRAS, APROVADOR, AUDITOR |
| `analista` | `analista` | ANALISTA |
| `auditor` | `auditor` | AUDITOR, CONSULTA |

Não reutilize essas credenciais ou o realm de desenvolvimento em ambientes compartilhados ou de produção.

---

# 📁 Estrutura do Repositório

```text
gpe-bbts-squad26/
│
├── docker-compose.yml
├── README.md
├── docs/
│   └── Documentação do projeto
│
├── gpe-backend/
│   ├── pom.xml
│   │
│   └── src/
│       └── main/
│           ├── java/
│           │   └── br/com/bbts/gpe/
│           │       └── Código-fonte do Back-end
│           │
│           └── resources/
│               ├── application.yml
│               └── db/
│                   └── migration/
│                       └── Migrações Flyway
│
└── gpe-frontend/
    ├── package.json
    │
    └── src/
        ├── components/
        │   └── Componentes visuais globais
        │
        ├── pages/
        │   └── Módulos e páginas do sistema
        │
        ├── services/
        │   └── Integrações com a API
        │
        └── routes/
            └── Rotas e proteção de acesso
```

---

# 💻 Pré-requisitos

Antes de executar o projeto, certifique-se de possuir as seguintes ferramentas instaladas:

* [Git](https://git-scm.com/)
* [Docker Desktop](https://www.docker.com/products/docker-desktop/)
* **JDK 21**
* **Node.js 18 ou superior**
* IDE de sua preferência:

  * Visual Studio Code
  * IntelliJ IDEA
  * Eclipse
  * ou outra IDE compatível

> ⚠️ O **Docker Desktop deve estar aberto e em execução** para iniciar os serviços do projeto.

---

# 🚀 Executando o Projeto

## 1. Clonar o repositório

Abra o terminal e execute:

```bash
git clone https://github.com/andrewgoncalvesabreu/gpe-bbts-squad26.git
```

Entre na pasta do projeto:

```bash
cd gpe-bbts-squad26
```

---

## 2. Subir Banco de Dados e Keycloak

Certifique-se de que o **Docker Desktop** está aberto.

Na raiz do projeto, execute:

```bash
docker compose up -d postgres-gpe keycloak-gpe
```

Esse comando irá iniciar os containers do **PostgreSQL 16** e do **Keycloak**. O realm `gpe` será importado automaticamente na primeira inicialização.

### Verificar o container

Para verificar se o banco está executando:

```bash
docker ps
```

Os containers `postgres-gpe` e `keycloak-gpe` deverão aparecer com status semelhante a:

```text
Up
```

---

## 3. Executar o Back-end

Abra um terminal na raiz do projeto e entre na pasta do Back-end:

```bash
cd gpe-backend
```

### Windows — PowerShell / CMD

```powershell
.\mvnw spring-boot:run
```

### Linux / macOS / Git Bash

```bash
./mvnw spring-boot:run
```

Durante a inicialização, o Maven Wrapper irá:

1. Baixar as dependências necessárias;
2. Inicializar a aplicação Spring Boot;
3. Conectar ao PostgreSQL;
4. Executar as migrações do Flyway;
5. Disponibilizar a API.

Por padrão, a API estará disponível em:

```text
http://localhost:8081
```

---

## 4. Executar o Front-end

Abra **outro terminal**, mantendo o Back-end em execução.

A partir da raiz do projeto:

```bash
cd gpe-frontend
```

Instale as dependências:

```bash
npm install
```

Depois, inicie o servidor de desenvolvimento:

```bash
npm run dev
```

O Vite exibirá no terminal o endereço da aplicação.

Por padrão:

```text
http://localhost:5173
```

Abra o endereço no navegador para acessar o sistema.

---

# 🔄 Fluxo da Aplicação

De forma simplificada, o funcionamento local do projeto é:

```text
                    ┌─────────────────┐
                    │    Navegador    │
                    │ React + Vite    │
                    └────────┬────────┘
                             │
                             │ HTTP / REST
                             ▼
                    ┌─────────────────┐
                    │    Back-end     │
                    │ Spring Boot 3   │
                    │    Java 21      │
                    └────────┬────────┘
                             │
                             │ JPA / JDBC
                             ▼
                    ┌─────────────────┐
                    │   PostgreSQL    │
                    │       16        │
                    └─────────────────┘
```

O **Front-end** realiza as requisições para a API do **Back-end**, que é responsável pelas regras de negócio e comunicação com o **PostgreSQL**.

---

# 🗄️ Banco de Dados e Flyway

As alterações no banco de dados são controladas através do **Flyway**.

As migrações ficam localizadas em:

```text
gpe-backend/src/main/resources/db/migration/
```

Cada alteração estrutural deve ser registrada através de uma nova migration SQL.

Exemplo:

```text
V1__create_tables.sql
V2__create_parceiros.sql
V3__create_oportunidades.sql
```

> ⚠️ Evite alterar migrations que já foram executadas em ambientes compartilhados. Para novas alterações, crie uma nova migration.

### Erro de checksum em banco de desenvolvimento antigo

Algumas versões anteriores usavam V8 para o histórico do parceiro e V9 para documentos, enquanto a sequência atual usa essas versões em ordem diferente. Se o backend informar checksum divergente nas migrações V8, V9 ou V11, faça backup do banco e, após confirmar que essas versões foram aplicadas com sucesso, execute `flyway repair` uma única vez usando a mesma URL e credenciais do PostgreSQL configuradas para o backend. O `repair` só atualiza os metadados do Flyway; a migração V12 então normaliza o esquema antigo dos documentos preservando os registros e arquivos. Não apague o volume do PostgreSQL nem edite a tabela `flyway_schema_history` manualmente.

---

# 🛠️ Comandos Úteis

## Parar o PostgreSQL

Para parar o container sem remover os dados:

```bash
docker-compose stop postgres-gpe
```

---

## Iniciar novamente o PostgreSQL

```bash
docker-compose start postgres-gpe
```

---

## Ver containers em execução

```bash
docker ps
```

---

## Resetar completamente o banco

> ⚠️ **Atenção:** esse procedimento remove os volumes do Docker e, consequentemente, os dados armazenados no banco.

```bash
docker-compose down -v
```

Depois, suba novamente:

```bash
docker-compose up -d postgres-gpe
```

O Flyway executará novamente as migrations durante a inicialização do Back-end.

---

## Problema de permissão no Maven Wrapper

Caso o Linux/macOS apresente erro de permissão ao executar o `mvnw`:

```bash
chmod +x gpe-backend/mvnw
```

Depois:

```bash
cd gpe-backend
./mvnw spring-boot:run
```

---

# 🌿 Fluxo de Trabalho com Git

Para manter o projeto organizado, **não realize commits diretamente na branch `main`**.

Cada tarefa deve ser desenvolvida em uma branch própria.

## Criar uma branch

```bash
git checkout -b feature/nome-da-sua-tarefa
```

Exemplo:

```bash
git checkout -b feature/cadastro-parceiro
```

---

## Atualizar sua branch com a `main`

Primeiro, atualize a `main`:

```bash
git checkout main
git pull origin main
```

Depois, volte para sua branch:

```bash
git checkout feature/nome-da-sua-tarefa
```

E faça o merge:

```bash
git merge main
```

---

# 📝 Padrão de Commits

Utilizamos um padrão baseado em **Conventional Commits**.

| Prefixo     | Utilização                                    |
| ----------- | --------------------------------------------- |
| `feat:`     | Nova funcionalidade                           |
| `fix:`      | Correção de bug                               |
| `docs:`     | Alterações na documentação                    |
| `refactor:` | Refatoração sem alteração de regra de negócio |
| `test:`     | Criação ou alteração de testes                |
| `chore:`    | Tarefas de manutenção/configuração            |

### Exemplos

```bash
git commit -m "feat: adiciona cadastro de parceiros"
```

```bash
git commit -m "fix: corrige validação do formulário"
```

```bash
git commit -m "docs: atualiza instruções de instalação"
```

```bash
git commit -m "refactor: reorganiza camada de serviços"
```

---

# 🔀 Pull Request

Após finalizar uma tarefa:

```bash
git add .
git commit -m "feat: descrição da alteração"
git push origin feature/nome-da-sua-tarefa
```

Em seguida, abra um **Pull Request** da sua branch para a `main`.

### Fluxo resumido

```text
main
 │
 ├── feature/minha-tarefa
 │       │
 │       ├── Desenvolvimento
 │       ├── Commit
 │       └── Push
 │
 └──────────────► Pull Request
                       │
                       ▼
                     main
```

---

# 📚 Documentação

A documentação complementar do projeto está disponível no diretório:

```text
/docs
```

Nesse diretório podem ser encontrados documentos relacionados a:

* Requisitos
* Regras de negócio
* Arquitetura
* Protótipos de telas
* Casos de uso
* Modelagem
* Normas e processos do sistema

---

# 👥 Projeto

**SIGES-PES / GPE — Sistema de Gestão de Parcerias Estratégicas**

**BB Tecnologia e Serviços (BBTS)**
**Projeto Squad 26 — Takeoff**

---

<p align="center">
  Desenvolvido pela <strong>Squad 26</strong> 🚀
</p>
