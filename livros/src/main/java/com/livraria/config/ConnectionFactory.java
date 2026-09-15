package com.livraria.config;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Fábrica de conexões JDBC com o MySQL.
 * Carrega propriedades de 'db.properties' com fallback para variáveis de ambiente e valores padrão.
 */
public class ConnectionFactory {

    private static String url;
    private static String user;
    private static String password;
    private static String driver;

    static {
        Properties props = new Properties();
        try (InputStream input = ConnectionFactory.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input != null) {
                props.load(input);
            }
        } catch (Exception e) {
            System.err.println("Aviso: Não foi possível ler db.properties. Usando padrões.");
        }

        url = System.getenv("DB_URL");
        if (url == null || url.trim().isEmpty()) {
            url = props.getProperty("db.url", "jdbc:mysql://localhost:3306/db_recomendador_livros?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8");
        }

        user = System.getenv("DB_USER");
        if (user == null || user.trim().isEmpty()) {
            user = props.getProperty("db.user", "root");
        }

        password = System.getenv("DB_PASSWORD");
        if (password == null) {
            password = props.getProperty("db.password", "rootpassword");
        }

        driver = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            System.err.println("Driver JDBC do MySQL não encontrado no classpath: " + driver);
        }
    }

    private ConnectionFactory() {
    }

    /**
     * Obtém uma nova conexão ativa com o banco de dados.
     * @return Connection JDBC
     * @throws SQLException Caso a conexão falhe
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Testa se a conexão com o banco de dados está operando.
     * @return true se conectou com sucesso, false caso contrário.
     */
    public static boolean testarConexao() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public static String getUrl() {
        return url;
    }

    public static String getUser() {
        return user;
    }

    /**
     * Encerra graciosamente a thread de limpeza do MySQL Connector/J para finalização limpa do processo.
     */
    public static void encerrarDriver() {
        try {
            com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.checkedShutdown();
        } catch (Exception ignored) {
        }
    }
}

