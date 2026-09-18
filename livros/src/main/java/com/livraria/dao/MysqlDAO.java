package com.livraria.dao;

import com.livraria.config.MysqlSingleton;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Classe base dos DAOs. Centraliza o acesso ao singleton de banco.
 */
public abstract class MysqlDAO {

    protected final MysqlSingleton banco;

    protected MysqlDAO() {
        this.banco = MysqlSingleton.getInstance();
    }

    protected Connection obterConexao() throws SQLException {
        return banco.obterConexao();
    }
}
