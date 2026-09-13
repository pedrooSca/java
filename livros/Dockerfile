# Imagem oficial do Apache Tomcat 9 com Java 25
FROM tomcat:9.0-jdk25-temurin

# Limpa os exemplos padrão do Tomcat para servir o projeto na raiz
RUN rm -rf /usr/local/tomcat/webapps/*

# Copia o pacote WAR da aplicação como ROOT.war
COPY target/livros.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080

CMD ["catalina.sh", "run"]
