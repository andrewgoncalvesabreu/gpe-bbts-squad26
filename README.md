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
* 📊 Avaliação e acompanhamento de oportunidades
* 📋 Gestão de informações e documentos
* 📈 Dashboards e indicadores gerenciais
* 🔐 Controle de acesso e autenticação
* 🏛️ Governança e conformidade com normas corporativas

---

# 🏗️ Arquitetura

O projeto utiliza uma arquitetura em **Monorepo**, mantendo Back-end, Front-end e Banco de Dados organizados dentro de um único repositório.

### Tecnologias utilizadas

| Camada                    | Tecnologia                 |
| ------------------------- | -------------------------- |
| ⚙️ Back-end               | Java 21 LTS                |
| 🚀 Framework              | Spring Boot 3.x            |
| 🗄️ Banco de Dados        | PostgreSQL 16              |
| 🔄 Migrações              | Flyway                     |
| 🔐 Segurança              | Spring Security + Keycloak |
| 🌐 Front-end              | React + TypeScript         |
| ⚡ Build Front-end         | Vite                       |
| 🎨 UI                     | PrimeReact + Tailwind CSS  |
| 🐳 Infraestrutura         | Docker + Docker Compose    |
| 📡 Comunicação            | API REST / HTTP            |
| 📦 Gerenciamento Back-end | Maven                      |

### Back-end

O Back-end foi desenvolvido utilizando **Java 21** e **Spring Boot 3.x**, contando com:

* Spring Data JPA
* Spring Security
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

## 2. Subir o Banco de Dados

Certifique-se de que o **Docker Desktop** está aberto.

Na raiz do projeto, execute:

```bash
docker-compose up -d postgres-gpe
```

Esse comando irá iniciar o container do **PostgreSQL 16**.

### Verificar o container

Para verificar se o banco está executando:

```bash
docker ps
```

O container `postgres-gpe` deverá aparecer com status semelhante a:

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
