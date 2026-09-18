# Sistema de Recomendação de Livros

Aplicação Java em arquitetura MVC para cadastro, consulta e recomendação de livros, com front-end em JSP e persistência em MySQL. O projeto foi estruturado para rodar em ambiente local com Maven e também pode ser executado com Docker Compose.

## Tecnologias

- Java 25
- Maven
- JSP + Servlets
- MySQL 8
- Docker / Docker Compose
- Tomcat 9 (execução em WAR)

## Estrutura do projeto

- [docker-compose.yml](docker-compose.yml): configuração dos containers da aplicação e do banco
- [livros](livros): projeto principal em Java/Maven
- [livros/src/main/java](livros/src/main/java): código fonte da aplicação
- [livros/src/main/webapp](livros/src/main/webapp): páginas JSP e recursos web
- [sql/init.sql](sql/init.sql): script de inicialização do banco de dados
- [livros/Dockerfile](livros/Dockerfile): imagem da aplicação web

## Funcionalidades

- cadastro de usuários
- cadastro de livros
- cadastro de gêneros
- avaliações
- recomendações por usuário
- página inicial e navegação via servlets

## Pré-requisitos

- Java 25
- Maven 3.9+
- Docker e Docker Compose (opcional, para ambiente em container)
- MySQL em execução local ou via Docker

## Execução via Docker

No diretório raiz do projeto:

```bash
docker compose up --build
```

A aplicação fica disponível em:

```text
http://localhost:8080
```

O banco fica disponível em:

```text
localhost:3306
```

## Configuração do banco

A conexão do projeto é configurada no arquivo [docker-compose.yml](docker-compose.yml). Os valores padrão do ambiente são:

- Banco: `db_recomendador_livros`
- Usuário: `root`
- Senha: `rootpassword`

O script de criação inicial está em [sql/init.sql](sql/init.sql).

## Build local

Para compilar e empacotar o projeto localmente:

```bash
cd livros
mvn clean package
```

O artefato gerado será:

```text
livros/target/livros.war
```

## Validação atual

O projeto foi validado com build real do Maven e compilou com sucesso:

```bash
cd "c:/Users/vivis/Downloads/java/livros"; & "c:/Users/vivis/Downloads/java/apache-maven-3.9.16/bin/mvn.cmd" clean package
```

Resultado verificado:

- `BUILD SUCCESS`
- `Tests run: 9, Failures: 0, Errors: 0, Skipped: 0`

## Observações

- A estrutura usa camadas MVC com servlets, services e DAO, o que é comum em projetos Java web.
- O projeto está em estado funcional para apresentação e entrega, desde que o ambiente de banco e aplicação esteja configurado corretamente.
- O diretório [livros/target](livros/target) será gerado após o build.
