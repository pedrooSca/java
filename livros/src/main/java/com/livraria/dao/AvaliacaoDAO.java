package com.livraria.dao;

import com.livraria.config.ConnectionFactory;
import com.livraria.model.Avaliacao;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO para gerenciamento da tabela 'avaliacoes'.
 */
public class AvaliacaoDAO {

    public AvaliacaoDAO() {
        garantirTabelaExiste();
    }

    /**
     * Garante a criação da tabela avaliacoes caso ela ainda não exista.
     */
    public void garantirTabelaExiste() {
        String sql = "CREATE TABLE IF NOT EXISTS avaliacoes (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "usuario_id INT NOT NULL, " +
                "livro_id INT NOT NULL, " +
                "nota INT NOT NULL, " +
                "comentario TEXT, " +
                "data_avaliacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "CONSTRAINT fk_avaliacoes_usuarios FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE, " +
                "CONSTRAINT fk_avaliacoes_livros FOREIGN KEY (livro_id) REFERENCES livros(id) ON DELETE CASCADE" +
                ") ENGINE=InnoDB";

        try (Connection conn = ConnectionFactory.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            // Em caso de erro na inicialização (ex: DB offline temporário), loga sem interromper
            System.err.println("Aviso AvaliacaoDAO: Não foi possível verificar/criar tabela avaliacoes: " + e.getMessage());
        }
    }

    /**
     * Insere uma nova avaliação.
     */
    public Avaliacao inserir(Avaliacao avaliacao) throws SQLException {
        if (avaliacao.getNota() < 1 || avaliacao.getNota() > 5) {
            throw new IllegalArgumentException("A nota da avaliação deve ser entre 1 e 5 estrelas.");
        }

        String sql = "INSERT INTO avaliacoes (usuario_id, livro_id, nota, comentario) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, avaliacao.getUsuarioId());
            ps.setInt(2, avaliacao.getLivroId());
            ps.setInt(3, avaliacao.getNota());
            ps.setString(4, avaliacao.getComentario());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    avaliacao.setId(rs.getInt(1));
                }
            }
        }
        return avaliacao;
    }

    /**
     * Lista avaliações de um determinado livro com dados do usuário.
     */
    public List<Avaliacao> listarPorLivro(int livroId) throws SQLException {
        List<Avaliacao> lista = new ArrayList<>();
        String sql = "SELECT a.id, a.usuario_id, u.nome AS usuario_nome, a.livro_id, l.titulo AS livro_titulo, " +
                "a.nota, a.comentario, a.data_avaliacao " +
                "FROM avaliacoes a " +
                "INNER JOIN usuarios u ON a.usuario_id = u.id " +
                "INNER JOIN livros l ON a.livro_id = l.id " +
                "WHERE a.livro_id = ? ORDER BY a.data_avaliacao DESC";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, livroId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Avaliacao a = new Avaliacao(
                            rs.getInt("id"),
                            rs.getInt("usuario_id"),
                            rs.getInt("livro_id"),
                            rs.getInt("nota"),
                            rs.getString("comentario"),
                            rs.getTimestamp("data_avaliacao")
                    );
                    a.setUsuarioNome(rs.getString("usuario_nome"));
                    a.setLivroTitulo(rs.getString("livro_titulo"));
                    lista.add(a);
                }
            }
        }
        return lista;
    }

    /**
     * Lista as avaliações mais recentes cadastradas no sistema.
     */
    public List<Avaliacao> listarTodas(int limite) throws SQLException {
        List<Avaliacao> lista = new ArrayList<>();
        String sql = "SELECT a.id, a.usuario_id, u.nome AS usuario_nome, a.livro_id, l.titulo AS livro_titulo, " +
                "a.nota, a.comentario, a.data_avaliacao " +
                "FROM avaliacoes a " +
                "INNER JOIN usuarios u ON a.usuario_id = u.id " +
                "INNER JOIN livros l ON a.livro_id = l.id " +
                "ORDER BY a.data_avaliacao DESC LIMIT ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limite > 0 ? limite : 50);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Avaliacao a = new Avaliacao(
                            rs.getInt("id"),
                            rs.getInt("usuario_id"),
                            rs.getInt("livro_id"),
                            rs.getInt("nota"),
                            rs.getString("comentario"),
                            rs.getTimestamp("data_avaliacao")
                    );
                    a.setUsuarioNome(rs.getString("usuario_nome"));
                    a.setLivroTitulo(rs.getString("livro_titulo"));
                    lista.add(a);
                }
            }
        }
        return lista;
    }

    /**
     * Retorna a média de notas de um livro específico. Retorna 0.0 se não houver avaliações.
     */
    public double obterMediaPorLivro(int livroId) throws SQLException {
        String sql = "SELECT AVG(nota) as media FROM avaliacoes WHERE livro_id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, livroId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("media");
                }
            }
        }
        return 0.0;
    }

    /**
     * Retorna um mapa com as médias de notas agrupadas por ID do livro.
     */
    public Map<Integer, Double> obterMediasPorLivros() throws SQLException {
        Map<Integer, Double> mapa = new HashMap<>();
        String sql = "SELECT livro_id, AVG(nota) as media FROM avaliacoes GROUP BY livro_id";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                mapa.put(rs.getInt("livro_id"), rs.getDouble("media"));
            }
        }
        return mapa;
    }

    /**
     * Retorna a quantidade total de avaliações registradas.
     */
    public int contarTotal() throws SQLException {
        String sql = "SELECT COUNT(*) FROM avaliacoes";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
}
