package com.livraria.dao;

import com.livraria.model.LogAlteracaoLivro;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para consulta da tabela de auditoria 'log_alteracoes_livros'
 * gerada automaticamente pelas Triggers de UPDATE e DELETE.
 */
public class LogAuditoriaDAO extends MysqlDAO {

    public List<LogAlteracaoLivro> listarLogs() throws SQLException {
        List<LogAlteracaoLivro> logs = new ArrayList<>();
        String sql = "SELECT id, livro_id, acao, titulo_anterior, titulo_novo, data_alteracao " +
                     "FROM log_alteracoes_livros ORDER BY id DESC";

        try (Connection conn = obterConexao();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                LogAlteracaoLivro log = new LogAlteracaoLivro(
                        rs.getInt("id"),
                        rs.getInt("livro_id"),
                        rs.getString("acao"),
                        rs.getString("titulo_anterior"),
                        rs.getString("titulo_novo"),
                        rs.getTimestamp("data_alteracao")
                );
                logs.add(log);
            }
        }
        return logs;
    }
}
