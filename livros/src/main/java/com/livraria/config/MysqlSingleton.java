package com.livraria.config;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Ponto unico de acesso a conexoes JDBC usado pelos DAOs.
 *
 * A conexao continua sendo aberta por operacao para evitar compartilhar uma
 * mesma Connection entre requisicoes concorrentes do Tomcat.
 */
public final class MysqlSingleton {

    private static final MysqlSingleton INSTANCE = new MysqlSingleton();

    private MysqlSingleton() {
    }

    public static MysqlSingleton getInstance() {
        return INSTANCE;
    }

    public Connection obterConexao() throws SQLException {
        return ConnectionFactory.getInstance().abrirConexao();
    }
}
