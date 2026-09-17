# Sistema de Recomendação de Livros

Este projeto é uma aplicação Java em arquitetura MVC para gestão e recomendação de livros, com frontend em JSP e banco de dados MySQL. A aplicação pode ser executada em containers com Docker Compose para facilitar o ambiente de desenvolvimento.

## Tecnologias

- Java 25
- Maven
- Apache Tomcat 9
- MySQL 8
- Docker / Docker Compose
- JSP + servlet-based MVC

## Estrutura do projeto

- [docker-compose.yml](docker-compose.yml): define os serviços do banco e da aplicação
- [livros](livros): projeto Java/Maven da aplicação
- [sql/init.sql](sql/init.sql): script de inicialização do banco
- [livros/Dockerfile](livros/Dockerfile): imagem da aplicação web

## Como executar

### 1) Pré-requisitos

- Docker instalado
- Docker Compose instalado

### 2) Subir o ambiente

No diretório raiz do projeto, execute:

```bash
docker compose up --build
```

Isso irá subir:

- banco MySQL em localhost:3306
- aplicação web em http://localhost:8080

### 3) Acessar a aplicação

Abra no navegador:

```text
http://localhost:8080
```

## Configuração do banco

A conexão com o banco é configurada via variáveis de ambiente no arquivo [docker-compose.yml](docker-compose.yml):

- Banco: `db_recomendador_livros`
- Usuário: `root`
- Senha: `rootpassword`

O script de criação inicial está em [sql/init.sql](sql/init.sql).

## Build local da aplicação

Se quiser compilar a aplicação diretamente:

```bash
cd livros
mvn clean package
```

## Observações

- A aplicação é empacotada em WAR e executada no Tomcat.
- O projeto foi pensado para facilitar a execução em ambiente local e de testes com containers.
- O diretório [livros/target](livros/target) será gerado após o build.
