package com.livraria.dao;

import com.livraria.model.LivroDetalhadoDTO;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO especializado na execução da Stored Procedure 'sp_obter_recomendacoes_usuario'.
 */
public class RecomendacaoDAO extends MysqlDAO {

    /**
     * Executa a Stored Procedure 'sp_obter_recomendacoes_usuario' no MySQL.
     * Retorna a lista de livros recomendados para o usuário informado com base em suas preferências.
     *
     * @param usuarioId ID do usuário
     * @return Lista de livros recomendados
     * @throws SQLException Em caso de erro na execução JDBC
     */
    public List<LivroDetalhadoDTO> obterRecomendacoesPorUsuario(int usuarioId) throws SQLException {
        List<LivroDetalhadoDTO> recomendacoes = new ArrayList<>();
        String callSql = "{CALL sp_obter_recomendacoes_usuario(?)}";

        try (Connection conn = obterConexao();
             CallableStatement cs = conn.prepareCall(callSql)) {

            cs.setInt(1, usuarioId);

            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    LivroDetalhadoDTO dto = new LivroDetalhadoDTO();
                    dto.setLivroId(rs.getInt("livro_id"));
                    dto.setTitulo(rs.getString("titulo"));
                    dto.setAutor(rs.getString("autor"));
                    dto.setAnoPublicacao((Integer) rs.getObject("ano_publicacao"));
                    dto.setGeneroNome(rs.getString("genero_nome"));
                    recomendacoes.add(dto);
                }
            }
        }
        return recomendacoes;
    }

    /**
     * Alias/conveniência para obterRecomendacoesPorUsuario.
     *
     * @param usuarioId ID do usuário
     * @return Lista de livros recomendados
     * @throws SQLException Em caso de erro na execução JDBC
     */
    public List<LivroDetalhadoDTO> obterRecomendacoes(int usuarioId) throws SQLException {
        return obterRecomendacoesPorUsuario(usuarioId);
    }
}
