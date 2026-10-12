## 📖 Sobre o Projeto

O **HuNails** é um sistema projetado para digitalizar e otimizar o mercado de beleza voltado para manicures. Este repositório contém o **backend** da aplicação, construído com arquitetura em camadas e focado em segurança, performance e escalabilidade.

A API gerencia desde a autenticação de usuários (clientes e manicures) até fluxos complexos de agendamento de horários, perfis públicos dinâmicos, publicações de portfólio (feed) e sistema de interações sociais (curtidas e seguidores).

## 🚀 Tecnologias e Ferramentas

* **Linguagem:** Java 17

* **Framework:** Spring Boot 3.2.3

* **Segurança:** Spring Security + JWT (JSON Web Tokens)

* **Persistência de Dados:** Spring Data JPA / Hibernate

* **Banco de Dados:** PostgreSQL (Produção) e H2 Database (Testes em memória)

* **Documentação:** Springdoc OpenAPI (Swagger)

* **Deploy & Infraestrutura:** Docker, Multi-stage builds

## 🏗️ Arquitetura e Padrões

O projeto adota o padrão de arquitetura em camadas MVC/REST, garantindo separação de responsabilidades e código limpo:

* **`controller/`**: Exposição dos endpoints REST e roteamento HTTP.

* **`service/`**: Isolamento de todas as regras de negócio.

* **`repository/`**: Interfaces do Spring Data para abstração do banco de dados.

* **`model/`**: Entidades mapeadas para o banco relacional (ORM).

* **`DTO/`**: Padrão *Data Transfer Object* rigorosamente implementado para blindar entidades e evitar vazamento de dados sensíveis (ex: senhas e hashes nunca chegam ao client).

* **`security/`**: Filtros de autenticação stateless e interceptação de rotas.

## 🔒 Segurança e Regras de Negócio

A API foi projetada com fortes requisitos de segurança:

* **Autorização Baseada em Regras:** Modificações de perfil (`PUT /usuarios/{id}`) e manipulação de agendamentos (`DELETE /agendamentos/{id}`) só podem ser feitas pelos próprios donos do recurso (retorna HTTP `403 Forbidden` em tentativas de violação).

* **Filtragem de Perfis Públicos:** Endpoints como `GET /usuarios` expõem apenas dados de contato comercial para manicures (ex: WhatsApp, localização), enquanto dados privados de clientes (CEP, idade, email) são omitidos publicamente.

* **Tratamento Global de Exceções:** Respostas HTTP padronizadas (`400 Bad Request`, `404 Not Found`, `409 Conflict` para agendamentos simultâneos).

## 📄 Documentação da API (Swagger)

A API é totalmente auto-documentada usando OpenAPI. Com a aplicação em execução, acesse a interface interativa do Swagger pelo navegador para testar os endpoints:

👉 [**https://backend-hunails.onrender.com/swagger-ui/index.html**](https://backend-hunails.onrender.com/swagger-ui/index.html)

*(O JSON completo do OpenAPI também pode ser acessado em `/v3/api-docs`)*

## 🛠️ Como Executar Localmente

### Pré-requisitos

* JDK 17

* PostgreSQL instalado (ou via Docker)

* Git

### 1. Clonar o repositório

```
git clone https://github.com/davs22/backend_hunails.git
cd backend_hunails

```

### 2. Configurar o Ambiente

Crie um banco de dados no PostgreSQL (ex: `hunails_db`). O projeto não utiliza arquivos `.env` diretamente para maior segurança. Exporte as variáveis de ambiente no seu terminal ou configure-as na sua IDE copiando o modelo do arquivo `.env.example`:

**No Windows (PowerShell):**

```
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/hunails_db"
$env:SPRING_DATASOURCE_USERNAME="postgres"
$env:SPRING_DATASOURCE_PASSWORD="sua_senha"
$env:JWT_SECRET="sua_chave_secreta_com_mais_de_32_caracteres"

```

**No Linux/macOS:**

```
export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/hunails_db"
export SPRING_DATASOURCE_USERNAME="postgres"
export SPRING_DATASOURCE_PASSWORD="sua_senha"
export JWT_SECRET="sua_chave_secreta_com_mais_de_32_caracteres"

```

### 3. Executar o Projeto (Maven Wrapper)

Com as variáveis definidas no terminal, rode a aplicação:

**Windows:** `.\mvnw.cmd spring-boot:run`
**Linux/Mac:** `./mvnw spring-boot:run`

*Nota: O projeto também inclui uma suíte de testes com banco em memória H2 que pode ser rodada via `./mvnw test`.*

## 🐳 Executando com Docker

Se preferir rodar sem precisar instalar o Java na sua máquina local, o projeto conta com um `Dockerfile` otimizado (Multi-stage build).

1. Construa a imagem:

```
docker build -t hunails-backend .

```

2. Execute o contêiner passando as variáveis de ambiente:

```
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL="jdbc:postgresql://host.docker.internal:5432/hunails_db" \
  -e SPRING_DATASOURCE_USERNAME="postgres" \
  -e SPRING_DATASOURCE_PASSWORD="sua_senha" \
  -e JWT_SECRET="sua_chave_secreta" \
  hunails-backend

```

## 📞 Contato

Desenvolvido por **Matheus Diirr** e equipe HuNails. Se você gostou deste projeto, sinta-se à vontade para se conectar comigo através do Linkedln: www.linkedin.com/in/matheus-diirr !
