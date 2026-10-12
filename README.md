# HuNails — backend

API de cadastro e autenticação, perfis de clientes e manicures, busca de manicures, publicações, interações, seguimento, agendamentos, denúncias e eventos de analytics. O código da aplicação está em `manicure_backend/`.

## Tecnologias e organização

Java 17, Spring Boot 3.2.3, Spring Security, JWT, Spring Data JPA, PostgreSQL, Maven Wrapper e Springdoc OpenAPI. Em `src/main/java/com/example/manicure_backend/`, `controller` expõe a API, `service` contém regras de negócio, `repository` acessa dados, `model` define entidades, `DTO` define contratos HTTP e `security` trata autenticação e autorização. Os testes de integração usam H2 em memória.

## Executar localmente

Requisitos: JDK 17 ou superior, PostgreSQL disponível e acesso à rede na primeira execução do Maven Wrapper. O projeto compila para Java 17. Os testes desta revisão foram executados com JDK 21; a execução com JDK 17 não foi verificada neste ambiente.

1. Crie um banco local, por exemplo `hunails`, e um usuário com acesso a ele.
2. Defina `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` e `JWT_SECRET` no ambiente do processo. O segredo JWT deve ser aleatório e ter pelo menos 32 bytes. `JWT_EXPIRATION_MS` é opcional e vale 86400000 por padrão.
3. Entre em `manicure_backend/` e execute o wrapper.

Exemplo no PowerShell, com valores **fictícios** que devem ser substituídos por valores locais:

```powershell
$env:SPRING_DATASOURCE_URL='jdbc:postgresql://localhost:5432/hunails'
$env:SPRING_DATASOURCE_USERNAME='example_user'
$env:SPRING_DATASOURCE_PASSWORD='replace_with_local_password'
$env:JWT_SECRET='replace_with_a_random_secret_of_at_least_32_bytes'
cd manicure_backend
.\mvnw.cmd spring-boot:run
```

No Linux/macOS, use `export NOME=valor` e `./mvnw spring-boot:run`. O arquivo `.env.example` documenta nomes e formatos. **Spring Boot não carrega `.env` automaticamente**: exporte as variáveis no shell, configure a IDE ou use o painel de variáveis da plataforma. Não armazene segredos no Git. A aplicação falha ao iniciar se faltar configuração obrigatória.

```powershell
cd manicure_backend
.\mvnw.cmd test
.\mvnw.cmd package
```

Os testes ativam o perfil `test`, criam um H2 em memória e usam um segredo JWT fictício próprio. Não exigem PostgreSQL nem credenciais reais.

## Exemplos de API

Os exemplos usam dados fictícios. O endpoint de cadastro aceita campos opcionais de perfil e `especialidade` para criar uma manicure:

```http
POST /auth/register
Content-Type: application/json

{"nome":"Ana Exemplo","email":"ana@example.invalid","senha":"senha-local-ficticia","especialidade":"unhas"}
```

```http
POST /auth/login
Content-Type: application/json

{"email":"ana@example.invalid","senha":"senha-local-ficticia"}
```

Use o `token` retornado no cabeçalho `Authorization: Bearer <token>`:

```http
POST /agendamentos
Authorization: Bearer <token>
Content-Type: application/json

{"manicureId":2,"descricao":"Manicure","data":"20/12/2030","hora":"14:00"}
```

## Permissões e contratos

- `PUT /usuarios/{id}` exige JWT e permite apenas atualizar a própria conta. Não há papel administrativo no projeto. O ID do corpo não concede permissão; `email` enviado na atualização continua ignorado.
- `GET /usuarios`, `/usuarios/nome`, `/usuarios/manicures` e `/usuarios/{id}` permanecem públicos. Perfis públicos omitem email, CEP, idade e sexo. Dados de contato e localização de clientes também são omitidos; perfis de manicures mantêm telefone, WhatsApp, bairro/cidade/estado e coordenadas para descoberta e contato. A própria pessoa autenticada recebe seus dados completos. O campo `senha` foi removido de todas as respostas de perfil.
- `GET /agendamentos/meus` lista agendamentos da pessoa autenticada como cliente; `GET /agendamentos/minha-agenda` lista os dela como manicure. `GET /agendamentos/{id}` e `DELETE /agendamentos/{id}` permitem apenas o cliente ou a manicure associados. O cancelamento mantém o comportamento anterior de exclusão do registro.
- Todas as respostas de agendamento agora usam `AgendamentoDTO` com `idAgendamento`, `descricao`, `data`, `hora`, `valor`, `status`, `usuario` e `manicure`. Os usuários relacionados são DTOs sem senha/hash, com dados filtrados segundo a pessoa autenticada. O formato da data de **entrada** continua `dd/MM/yyyy` e o de **saída** continua `dd-MM-yyyy`; hora usa `HH:mm`.
- `POST /posts` agora responde com `PostDTO` (os campos dos endpoints de listagem de posts), sem a entidade `author` aninhada. A autoria fica nos campos `idAuthor`, `authorNome`, `authorFoto` e `regiao`.
- Falta de autenticação/JWT inválido ou expirado: 401. Acesso a conta ou agendamento alheio: 403. Registro inexistente: 404. Entrada inválida: 400. Horário ocupado: 409.

Com a aplicação em execução, a documentação Springdoc fica em `/swagger-ui/index.html` e o JSON OpenAPI em `/v3/api-docs`. A interface não substitui o cabeçalho JWT nos endpoints privados.

## Limitações e segurança operacional

Os testes H2 cobrem os fluxos HTTP, autenticação, autorização e serialização, mas H2 não reproduz integralmente o PostgreSQL (tipos, concorrência, índices e detalhes de SQL). Não houve teste contra PostgreSQL neste ambiente. A verificação de horário ocupado ocorre na aplicação e não impede sozinha uma corrida entre reservas simultâneas. `spring.jpa.hibernate.ddl-auto=update` serve para desenvolvimento; migrações versionadas são recomendáveis antes de uso em produção.

O repositório anteriormente rastreava `.env` com configurações de banco potencialmente válidas. Substitua a senha do banco e quaisquer credenciais associadas. Se o segredo JWT anteriormente usado foi implantado, substitua-o também, invalidando os tokens antigos. A remoção de arquivos da versão atual **não elimina valores do histórico Git**. Não foi reescrito o histórico nem feito force push.

## Créditos

HuNails e seus contribuidores, conforme o histórico Git do repositório. Esta documentação não atribui autoria individual além do que o histórico registra.
