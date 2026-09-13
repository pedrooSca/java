package com.livraria.dao;

import com.livraria.config.ConnectionFactory;
import com.livraria.model.Genero;
import com.livraria.model.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO para gerenciamento da tabela 'usuarios' e relacionamento N:M 'usuario_generos'.
 */
public class UsuarioDAO {

    public List<Usuario> listarTodos() throws SQLException {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT id, nome, email, data_cadastro FROM usuarios ORDER BY nome ASC";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Usuario u = new Usuario(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getString("email")
                );
                u.setDataCadastro(rs.getTimestamp("data_cadastro"));
                lista.add(u);
            }
        }

        // Carrega preferências para cada usuário
        for (Usuario u : lista) {
            u.setGenerosPreferidos(obterGenerosPreferidos(u.getId()));
        }

        return lista;
    }

    public Optional<Usuario> buscarPorId(int id) throws SQLException {
        String sql = "SELECT id, nome, email, data_cadastro FROM usuarios WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Usuario u = new Usuario(
                            rs.getInt("id"),
                            rs.getString("nome"),
                            rs.getString("email")
                    );
                    u.setDataCadastro(rs.getTimestamp("data_cadastro"));
                    u.setGenerosPreferidos(obterGenerosPreferidos(id));
                    return Optional.of(u);
                }
            }
        }
        return Optional.empty();
    }

    public Usuario inserir(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuarios (nome, email) VALUES (?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, usuario.getNome());
            ps.setString(2, usuario.getEmail());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    usuario.setId(rs.getInt(1));
                }
            }
        }
        return usuario;
    }

    public List<Genero> obterGenerosPreferidos(int usuarioId) throws SQLException {
        List<Genero> generos = new ArrayList<>();
        String sql = "SELECT g.id, g.nome, g.descricao FROM generos g " +
                     "INNER JOIN usuario_generos ug ON g.id = ug.genero_id " +
                     "WHERE ug.usuario_id = ? ORDER BY g.nome ASC";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    generos.add(new Genero(
                            rs.getInt("id"),
                            rs.getString("nome"),
                            rs.getString("descricao")
                    ));
                }
            }
        }
        return generos;
    }

    public void adicionarPreferencia(int usuarioId, int generoId) throws SQLException {
        String sql = "INSERT IGNORE INTO usuario_generos (usuario_id, genero_id) VALUES (?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, usuarioId);
            ps.setInt(2, generoId);
            ps.executeUpdate();
        }
    }

    public void removerPreferencia(int usuarioId, int generoId) throws SQLException {
        String sql = "DELETE FROM usuario_generos WHERE usuario_id = ? AND genero_id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, usuarioId);
            ps.setInt(2, generoId);
            ps.executeUpdate();
        }
    }
}
