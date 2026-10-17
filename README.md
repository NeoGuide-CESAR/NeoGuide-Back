# NeoGuide Backend — API REST

API RESTful do **NeoGuide**, plataforma para automação, dimensionamento e cálculo de demanda de projetos elétricos prediais e residenciais em conformidade com as normas técnicas da concessionária Neoenergia Pernambuco (**DIS-NOR-030** e **DIS-NOR-053**).

---

## 🛠️ Tecnologias Utilizadas

- **Java 21** (Eclipse Temurin)
- **Spring Boot 3** (Spring Data JPA, Spring Security, Validation)
- **PostgreSQL 16** (Banco de dados de produção e homologação)
- **H2 Database** (Banco em memória isolado para suíte de testes)
- **JJWT 0.12.6** (Autenticação stateless com tokens JWT HMAC-SHA256)
- **Docker & Docker Compose** (Empacotamento multi-stage e orquestração de ambiente)
- **Maven Wrapper** (Gerenciador de dependências e build automatizado)

---

## 📋 Pré-requisitos

Para executar a aplicação com Docker, você precisa apenas de:
- [Docker](https://docs.docker.com/get-docker/) (versão 20.10 ou superior)
- [Docker Compose](https://docs.docker.com/compose/install/) (versão 2.0 ou superior)

> **Nota:** Caso queira executar o projeto nativamente sem Docker, é necessário ter instalado o **JDK 21**.

---

## 🐳 Como Subir o Projeto via Docker

O repositório já conta com um manifesto `docker-compose.yml` e um `Dockerfile` multi-stage otimizado.

### 1. Iniciar os Containers

Na raiz do repositório, execute o comando abaixo para construir a imagem da API e subir todos os serviços em segundo plano:

```bash
docker compose up -d --build
```

O Docker Compose inicializará dois serviços orquestrados na rede interna `neoguide-net`:

| Serviço | Imagem | Porta Exposta | Descrição |
| :--- | :--- | :---: | :--- |
| **`neoguide-db`** | `postgres:16-alpine` | `5432` | Banco PostgreSQL com healthcheck via `pg_isready` e volume persistente |
| **`neoguide-api`** | Multi-stage JRE 21 | `8080` | Aplicação Spring Boot (inicia após o banco estar saudável) |

### 2. Acompanhar os Logs da Aplicação

Para acompanhar os logs de inicialização do backend em tempo real:

```bash
docker compose logs -f neoguide-api
```

Você verá a mensagem de inicialização do Spring Boot e a confirmação da conexão com o pool HikariCP:
```text
Started Application in X.XXX seconds
HikariPool-1 - Added connection ... url=jdbc:postgresql://neoguide-db:5432/neoguide_db
```

### 3. Verificar o Status dos Containers

Para checar a saúde e integridade dos serviços:

```bash
docker compose ps
```

Ambos os containers devem figurar como `Up` ou `healthy`.

### 4. Parar os Containers

Para pausar e encerrar os containers:

```bash
docker compose down
```

> **Dica:** Se desejar apagar os dados do volume persistente do banco para reiniciar a base do zero:
> ```bash
> docker compose down -v
> ```

---

## 🌱 Povoamento Automático das Normas Técnicas (DataSeeder)

Ao iniciar contra o PostgreSQL pela primeira vez, o componente `NormativeDataSeeder` popula automaticamente as **21 tabelas normativas** com as diretrizes das normas DIS-NOR-030 e DIS-NOR-053:
- **Quadros 33, 35, 36 e 37**: Fatores de demanda residencial, áreas mínimas e recarga de veículos elétricos (VE).
- **Tabelas 1, 2 e 3**: Dimensionamento de padrão de entrada e barramentos do QDG.
- **Tabelas 5 a 22**: Fatores de demanda de iluminação, TUGs, ar-condicionado, motores, aquecimento e áreas comuns.

O seeder é **idempotente** e só executa inserções caso as tabelas estejam vazias, sem duplicar registros em reinicializações.

---

## 📮 Testando os Endpoints (Postman Collection)

A API disponibiliza uma coleção Postman v2.1 pronta para uso com testes automatizados e exemplos de requisição e resposta em:
📁 [`postman/NeoGuide_API.postman_collection.json`](postman/NeoGuide_API.postman_collection.json)

### Principais Rotas Disponíveis (`http://localhost:8080/api/v1`):

#### 🔐 Autenticação (`/api/v1/auth`)
- `POST /register`: Cadastro de projetistas (`ROLE_PROJETISTA`) ou analistas (`ROLE_ANALISTA`) com CPF/CNPJ sanitizado.
- `POST /login`: Login flexível aceitando e-mail ou CPF/CNPJ (com ou sem máscara), retornando token Bearer JWT.

#### ⚡ Gestão de Projetos (`/api/v1/projects`)
- `POST /projects`: Cadastro do DNA do empreendimento (tensão de fornecimento, flags de carga comercial/VE/incêndio, localização) com protocolo anual sequencial (`PRJ-AAAA-XXXX`).
- `GET /projects`: Listagem paginada Spring Data (`?page=0&size=10&sort=createdAt,desc`) com filtros opcionais por status (`?status=RASCUNHO`) ou busca textual (`?search=Edificio`).
- `GET /projects/{id}`: Detalhe do projeto com isolamento estrito por usuário (retorna HTTP 403 Forbidden para terceiros).
- `POST /projects/{id}/duplicate`: Clonagem atômica de projeto em menos de 100ms, com novo protocolo e sufixo `(cópia)`.

---

## 🧪 Execução de Testes Automatizados (Local)

Para rodar a suíte completa de testes unitários e de integração sem precisar do Docker:

**No Windows (PowerShell / CMD):**
```powershell
.\mvnw.cmd test
```

**No Linux / macOS:**
```bash
./mvnw test
```

A suíte utiliza o perfil `@ActiveProfiles("test")` com banco H2 em memória em modo PostgreSQL, executando 100% isolada e sem dependências externas.
