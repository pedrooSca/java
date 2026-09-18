package com.livraria.dao;

import com.livraria.model.Livro;
import com.livraria.model.LivroDetalhadoDTO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO para gerenciamento da tabela 'livros' (CRUD) e consumo da View 'vw_livros_detalhados'.
 * As operações de UPDATE e DELETE ativam automaticamente as triggers de auditoria.
 */
public class LivroDAO extends MysqlDAO {

    /**
     * Insere um novo livro.
     */
    public Livro inserir(Livro livro) throws SQLException {
        String sql = "INSERT INTO livros (titulo, autor, isbn, ano_publicacao, genero_id) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = obterConexao();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, livro.getTitulo());
            ps.setString(2, livro.getAutor());
            ps.setString(3, livro.getIsbn());
            if (livro.getAnoPublicacao() != null) {
                ps.setInt(4, livro.getAnoPublicacao());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setInt(5, livro.getGeneroId());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    livro.setId(rs.getInt(1));
                }
            }
        }
        return livro;
    }

    /**
     * Atualiza um livro existente.
     * Aciona o Trigger 'trg_depois_atualizar_livro' no MySQL.
     */
    public boolean atualizar(Livro livro) throws SQLException {
        String sql = "UPDATE livros SET titulo = ?, autor = ?, isbn = ?, ano_publicacao = ?, genero_id = ? WHERE id = ?";
        try (Connection conn = obterConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, livro.getTitulo());
            ps.setString(2, livro.getAutor());
            ps.setString(3, livro.getIsbn());
            if (livro.getAnoPublicacao() != null) {
                ps.setInt(4, livro.getAnoPublicacao());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setInt(5, livro.getGeneroId());
            ps.setInt(6, livro.getId());

            int linhasAfetadas = ps.executeUpdate();
            return linhasAfetadas > 0;
        }
    }

    /**
     * Exclui um livro pelo ID.
     * Aciona o Trigger 'trg_depois_deletar_livro' no MySQL.
     */
    public boolean excluir(int id) throws SQLException {
        String sql = "DELETE FROM livros WHERE id = ?";
        try (Connection conn = obterConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            int linhasAfetadas = ps.executeUpdate();
            return linhasAfetadas > 0;
        }
    }

    /**
     * Busca um livro pela chave primária.
     */
    public Optional<Livro> buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, titulo, autor, isbn, ano_publicacao, genero_id, data_cadastro FROM livros WHERE id = ?";
        try (Connection conn = obterConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Livro livro = new Livro(
                            rs.getInt("id"),
                            rs.getString("titulo"),
                            rs.getString("autor"),
                            rs.getString("isbn"),
                            (Integer) rs.getObject("ano_publicacao"),
                            rs.getInt("genero_id")
                    );
                    livro.setDataCadastro(rs.getTimestamp("data_cadastro"));
                    return Optional.of(livro);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Lista todos os livros brutos da tabela 'livros'.
     */
    public List<Livro> listarTodos() throws SQLException {
        List<Livro> livros = new ArrayList<>();
        String sql = "SELECT id, titulo, autor, isbn, ano_publicacao, genero_id, data_cadastro FROM livros ORDER BY id ASC";

        try (Connection conn = obterConexao();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Livro livro = new Livro(
                        rs.getInt("id"),
                        rs.getString("titulo"),
                        rs.getString("autor"),
                        rs.getString("isbn"),
                        (Integer) rs.getObject("ano_publicacao"),
                        rs.getInt("genero_id")
                );
                livro.setDataCadastro(rs.getTimestamp("data_cadastro"));
                livros.add(livro);
            }
        }
        return livros;
    }

    /**
     * Consulta utilizando a VIEW 'vw_livros_detalhados'.
     * Retorna a listagem de livros com o nome do respectivo gênero já associado.
     */
    public List<LivroDetalhadoDTO> listarDetalhados() throws SQLException {
        List<LivroDetalhadoDTO> lista = new ArrayList<>();
        String sql = "SELECT livro_id, titulo, autor, isbn, ano_publicacao, genero_id, genero_nome FROM vw_livros_detalhados ORDER BY livro_id ASC";

        try (Connection conn = obterConexao();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                LivroDetalhadoDTO dto = new LivroDetalhadoDTO(
                        rs.getInt("livro_id"),
                        rs.getString("titulo"),
                        rs.getString("autor"),
                        rs.getString("isbn"),
                        (Integer) rs.getObject("ano_publicacao"),
                        rs.getInt("genero_id"),
                        rs.getString("genero_nome")
                );
                lista.add(dto);
            }
        }
        return lista;
    }

    /**
     * Consulta detalhada por ID a partir da VIEW.
     */
    public Optional<LivroDetalhadoDTO> buscarDetalhadoPorId(int id) throws SQLException {
        String sql = "SELECT livro_id, titulo, autor, isbn, ano_publicacao, genero_id, genero_nome FROM vw_livros_detalhados WHERE livro_id = ?";
        try (Connection conn = obterConexao();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new LivroDetalhadoDTO(
                            rs.getInt("livro_id"),
                            rs.getString("titulo"),
                            rs.getString("autor"),
                            rs.getString("isbn"),
                            (Integer) rs.getObject("ano_publicacao"),
                            rs.getInt("genero_id"),
                            rs.getString("genero_nome")
                    ));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Pesquisa livros na VIEW 'vw_livros_detalhados' por termo (título ou autor) e/ou gênero.
     *
     * @param termo Palavra-chave a buscar no título ou autor (opcional)
     * @param generoId ID do gênero literário (opcional)
     * @return Lista de livros correspondentes
     * @throws SQLException Em caso de erro JDBC
     */
    public List<LivroDetalhadoDTO> pesquisar(String termo, Integer generoId) throws SQLException {
        List<LivroDetalhadoDTO> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT livro_id, titulo, autor, isbn, ano_publicacao, genero_id, genero_nome FROM vw_livros_detalhados WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (termo != null && !termo.trim().isEmpty()) {
            sql.append("AND (LOWER(titulo) LIKE ? OR LOWER(autor) LIKE ? OR isbn LIKE ?) ");
            String like = "%" + termo.trim().toLowerCase() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }

        if (generoId != null && generoId > 0) {
            sql.append("AND genero_id = ? ");
            params.add(generoId);
        }

        sql.append("ORDER BY livro_id ASC");

        try (Connection conn = obterConexao();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LivroDetalhadoDTO dto = new LivroDetalhadoDTO(
                            rs.getInt("livro_id"),
                            rs.getString("titulo"),
                            rs.getString("autor"),
                            rs.getString("isbn"),
                            (Integer) rs.getObject("ano_publicacao"),
                            rs.getInt("genero_id"),
                            rs.getString("genero_nome")
                    );
                    lista.add(dto);
                }
            }
        }
        return lista;
    }

    /**
     * Retorna a contagem total de livros cadastrados.
     */
    public int contarTotal() throws SQLException {
        String sql = "SELECT COUNT(*) FROM livros";
        try (Connection conn = obterConexao();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
}
