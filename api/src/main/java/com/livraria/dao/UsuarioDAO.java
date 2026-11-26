package com.livraria.dao;

import com.livraria.model.Genero;
import com.livraria.model.Usuario;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** DAO para gerenciamento da tabela 'usuarios' e relacionamento N:M 'usuario_generos'. */
public class UsuarioDAO extends MysqlDAO {

  /** Resolve o leitor pelo e-mail da conta autenticada, nunca pelo ID enviado pelo cliente. */
  public Optional<Integer> obterUsuarioIdDaConta(int contaId) throws SQLException {
    try (var conexao = obterConexao()) {
      // Contas antigas e administrativas podem ainda não possuir cadastro de leitor.
      // A chave única de e-mail preserva o nome e o ID dos leitores existentes.
      try (var comando = conexao.prepareStatement(
          "INSERT INTO usuarios (nome, email) "
              + "SELECT SUBSTRING_INDEX(c.email, '@', 1), c.email FROM contas_acesso c WHERE c.id = ? "
              + "ON DUPLICATE KEY UPDATE email = usuarios.email")) {
        comando.setInt(1, contaId);
        comando.executeUpdate();
      }
      try (var comando = conexao.prepareStatement(
          "SELECT u.id FROM usuarios u JOIN contas_acesso c ON c.email = u.email WHERE c.id = ?")) {
        comando.setInt(1, contaId);
        try (var resultado = comando.executeQuery()) {
          return resultado.next() ? Optional.of(resultado.getInt("id")) : Optional.empty();
        }
      }
    }
  }

  public List<Usuario> listarTodos() throws SQLException {
    List<Usuario> lista = new ArrayList<>();
    String sql = "SELECT id, nome, email, data_cadastro FROM usuarios ORDER BY nome ASC";

    try (Connection conn = obterConexao();
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery()) {

      while (rs.next()) {
        Usuario u = new Usuario(rs.getInt("id"), rs.getString("nome"), rs.getString("email"));
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
    try (Connection conn = obterConexao();
        PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setInt(1, id);
      try (ResultSet rs = ps.executeQuery()) {
        if (rs.next()) {
          Usuario u = new Usuario(rs.getInt("id"), rs.getString("nome"), rs.getString("email"));
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
    try (Connection conn = obterConexao();
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
    String sql =
        "SELECT g.id, g.nome, g.descricao FROM generos g "
            + "INNER JOIN usuario_generos ug ON g.id = ug.genero_id "
            + "WHERE ug.usuario_id = ? ORDER BY g.nome ASC";

    try (Connection conn = obterConexao();
        PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setInt(1, usuarioId);
      try (ResultSet rs = ps.executeQuery()) {
        while (rs.next()) {
          generos.add(new Genero(rs.getInt("id"), rs.getString("nome"), rs.getString("descricao")));
        }
      }
    }
    return generos;
  }

  public void adicionarPreferencia(int usuarioId, int generoId) throws SQLException {
    String sql = "INSERT IGNORE INTO usuario_generos (usuario_id, genero_id) VALUES (?, ?)";
    try (Connection conn = obterConexao();
        PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setInt(1, usuarioId);
      ps.setInt(2, generoId);
      ps.executeUpdate();
    }
  }

  public void removerPreferencia(int usuarioId, int generoId) throws SQLException {
    String sql = "DELETE FROM usuario_generos WHERE usuario_id = ? AND genero_id = ?";
    try (Connection conn = obterConexao();
        PreparedStatement ps = conn.prepareStatement(sql)) {

      ps.setInt(1, usuarioId);
      ps.setInt(2, generoId);
      ps.executeUpdate();
    }
  }
}
