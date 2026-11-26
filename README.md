# Livraria — recomendação de livros

Projeto simples em Java, JSP e MySQL, separado em duas aplicações independentes:

- [livros/](./livros/): front-end (JSP, CSS e JavaScript) e mini backend que encaminha as requisições para a API. Não acessa o banco.
- [api/](./api/): API JSON, regras de negócio, modelos e DAOs. Não contém telas nem arquivos de front-end.
- [api/sql/init.sql](./api/sql/init.sql): criação do banco, dados iniciais, view, triggers e procedure de recomendações.
- [postman/](./postman/): coleção para testar os endpoints.

Fluxo: navegador → front-end e proxy → API → MySQL.

O sistema mantém cadastro, pesquisa, edição e exclusão de livros, cadastro de leitores, avaliações, preferências, recomendações e auditoria.

## Executar com Docker

1. Copie [.env.example](./.env.example) para `.env` e preencha:
   - `MYSQL_ROOT_PASSWORD`: senha do MySQL.
   - `ADMIN_EMAIL` e `ADMIN_PASSWORD`: acesso inicial do administrador (senha de 12 a 200 caracteres).
   - `JWT_SECRET`: segredo aleatório de pelo menos 32 bytes, usado somente pela API.

   O arquivo `.env` é local e não deve ser publicado. No PowerShell, um segredo pode ser gerado com `[guid]::NewGuid().ToString('N') + [guid]::NewGuid().ToString('N')`.
2. Na raiz, execute:

```sh
docker compose up --build -d
```

Abra o site em http://localhost:8080 e entre com `ADMIN_EMAIL` e `ADMIN_PASSWORD`. A API pode ser testada diretamente em http://localhost:8081/api/livros, enviando o JWT. A porta 8081 fica vinculada somente ao computador local para os testes com Postman.

### Primeiro acesso do administrador

O e-mail inicial sugerido é `admin@livraria.local`. A primeira senha é a que você definir em `ADMIN_PASSWORD` no `.env`; não existe uma senha fixa embutida no projeto. Para uma demonstração local, você pode configurar `ADMIN_PASSWORD=LivrariaAdmin123!` e entrar com essa senha. Use uma senha própria ao hospedar o sistema. As credenciais reais do ambiente local não são publicadas no repositório.

O banco é inicializado automaticamente na primeira execução, incluindo os gêneros necessários para cadastrar livros. Na inicialização da API, uma atualização idempotente também cria a tabela de acesso e insere os gêneros ausentes em bancos existentes. Ela não apaga registros nem redefine senhas de contas existentes. Os dados ficam no volume `db_data`. Para parar sem apagar os dados:

```sh
docker compose down
```

## Organização

```text
api/
  src/main/java/com/livraria/
    controller/                 # endpoints e contrato JSON
    security/                   # filtro JWT e hash de senhas
    service/                    # regras de negócio
    dao/                        # consultas JDBC
    model/                      # dados
    config/                     # conexão MySQL
  src/main/resources/          # db.properties e atualização idempotente do banco
  src/test/java/
  sql/
  pom.xml
  Dockerfile
livros/
  src/main/java/com/livraria/controller/
    PaginaServlet.java          # rotas das telas
    ApiProxyServlet.java        # ligação HTTP com a API
  src/main/webapp/              # login, JSP, CSS e JavaScript
  src/test/
  pom.xml
  Dockerfile
docker-compose.yml
```

Cada aplicação gera seu próprio WAR e pode rodar em um Tomcat 9 com Java 25 ou superior. Para instalar manualmente, use `ROOT.war` em dois Tomcats separados (front na porta 8080, API na porta 8081). A variável `API_URL` do front indica o endereço da API, sem `/api` no final; o padrão é `http://localhost:8081`. No Docker, ela já aponta para `http://api:8080`.

Apenas a API usa `DB_URL`, `DB_USER` e `DB_PASSWORD`. Os valores locais padrão estão em [db.properties](./api/src/main/resources/db.properties). O JavaScript chama `/api/...` no servidor do front, e o proxy encaminha método, parâmetros e JSON para o outro servidor.

## Endpoints principais

Todos usam o prefixo `/api`. As respostas com conteúdo usam JSON. Com exceção de `POST /auth/login`, `POST /auth/cadastro` e OPTIONS, todos exigem `Authorization: Bearer <token>`. POST e PUT com corpo exigem `Content-Type: application/json`.

| Método | Caminho | Operação |
| --- | --- | --- |
| POST | `/auth/login` | Autenticar e obter um JWT |
| POST | `/auth/cadastro` | Cadastrar leitor e conta CONSULTA, sem autenticação |
| POST | `/auth/contas` | Administrador cria outra conta de acesso |
| GET, POST | `/livros` | Pesquisar (`q`, `generoId`) ou cadastrar |
| GET, PUT, DELETE | `/livros/{id}` | Consultar, editar ou excluir |
| GET | `/generos` | Listar gêneros |
| GET, POST | `/usuarios` | Listar ou cadastrar leitores |
| GET | `/usuarios/{id}` | Consultar leitor |
| GET | `/usuarios/{id}/preferencias` | Listar preferências |
| PUT, DELETE | `/usuarios/{id}/preferencias/{generoId}` | Adicionar ou remover preferência |
| GET | `/usuarios/{id}/recomendacoes` | Recomendar livros |
| GET, POST | `/avaliacoes` | Listar (filtro `livroId`) ou cadastrar avaliação |
| GET | `/medias` | Médias das avaliações por livro |
| GET | `/auditoria` | Histórico de alterações |
| GET | `/resumo` | Dados da página inicial |

## Compilar e testar

Com Maven instalado e Java 25 ou superior:

```sh
mvn -f api/pom.xml clean package
mvn -f livros/pom.xml clean package
```

Os testes da API usam DAOs em memória. O teste do front compila as JSPs no Tomcat e verifica o proxy contra outro servidor HTTP, incluindo erros e API fora do ar.

O teste opcional com MySQL exige `RUN_MYSQL_TESTS=true` e `DB_URL` apontando para um banco separado cujo nome termine em `_test`, preparado com o SQL do projeto. Ele não usa o banco normal.

Se houver Node.js instalado, o teste de preferências do front pode ser executado com:

```sh
node livros/src/test/js/recomendacoes.test.mjs
node livros/src/test/js/autenticacao.test.mjs
node livros/src/test/js/avaliacoes.test.mjs
```

## Login e permissões

A página de login contém o formulário de cadastro com nome, e-mail e senha. `POST /api/auth/cadastro` cria o leitor e sua conta `CONSULTA` na mesma transação. Depois do cadastro, entre com o e-mail e a senha escolhidos. O perfil não pode ser escolhido pelo cadastro público.

As contas de acesso continuam separadas dos leitores no banco. O endpoint administrativo `POST /api/usuarios` cria somente o leitor, para compatibilidade com a coleção e os dados anteriores. O formulário de cadastro foi retirado da página de usuários, que mantém a listagem.

- `ADMIN`: consulta, cadastra, edita, exclui e visualiza auditoria.
- `CONSULTA`: consulta dados e publica avaliações; as demais alterações e a auditoria são exclusivas do administrador.

As avaliações são publicadas pelo leitor correspondente à conta autenticada, inclusive para administradores. A tela permite escolher apenas o livro, a nota e o comentário. `POST /api/avaliacoes` recebe `livroId`, `nota` e `comentario`; o autor é resolvido no servidor pelo token, ignorando qualquer `usuarioId` enviado no JSON. Contas antigas sem leitor associado recebem esse cadastro no primeiro envio, usando o e-mail da conta e preservando leitores já existentes.

Obtenha um token com `POST /api/auth/login`:

```json
{"email":"admin@livraria.local","senha":"sua senha configurada no ambiente"}
```

A resposta contém `token`, `tipo`, `expiraEm` (1800 segundos), `perfil` e `email`. Envie o token no header `Authorization: Bearer <token>`. A API retorna 401 para credenciais/token inválidos ou expirados e 403 quando o perfil não permite a operação. A API não usa sessão HTTP.

Para criar uma conta de consulta, um administrador envia o próprio token em `POST /api/auth/contas`:

```json
{"email":"consulta@exemplo.com","senha":"uma senha forte de exemplo","perfil":"CONSULTA"}
```

A resposta de criação é 201 sem corpo. Senhas usam PBKDF2-HMAC-SHA256 com salt aleatório e 600 mil iterações. Apenas o hash é salvo no MySQL. Os tokens são assinados com HS256 e expiram após 30 minutos. O payload não é criptografado e não contém senhas. Use HTTPS quando hospedar fora do ambiente local.

O front guarda o token em `sessionStorage`, que é separado por aba. Sair remove o token daquela aba; um token já emitido continua válido até expirar. A sessão da interface expirada volta ao login quando a API responde 401. O proxy encaminha `Authorization` e as respostas da API, sem acesso ao banco.

O administrador inicial só é criado quando o e-mail ainda não existe. Alterar `ADMIN_PASSWORD` no ambiente não redefine uma senha já persistida. Para uma troca, crie outra conta ADMIN autenticada, atualize a configuração e use a nova conta. Não remova o volume para atualizar a aplicação.

A coleção do Postman começa com login e salva o token automaticamente. Preencha `email` e `senha` no ambiente local do Postman, sem compartilhar credenciais reais.
