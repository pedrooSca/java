package com.livraria.dao;

import com.livraria.model.ContaAcesso;
import java.sql.*;
import java.util.Optional;

public class ContaAcessoDAO extends MysqlDAO {
  public Optional<ContaAcesso> buscarPorEmail(String email) throws SQLException {
    try (var conexao = obterConexao();
        var comando =
            conexao.prepareStatement(
                "SELECT id, email, senha_hash, perfil FROM contas_acesso WHERE email = ?")) {
      comando.setString(1, email);
      try (var resultado = comando.executeQuery()) {
        if (!resultado.next()) return Optional.empty();
        return Optional.of(
            new ContaAcesso(
                resultado.getInt("id"),
                resultado.getString("email"),
                resultado.getString("senha_hash"),
                resultado.getString("perfil")));
      }
    }
  }

  public void cadastrar(String email, String senhaHash, String perfil) throws SQLException {
    try (var conexao = obterConexao();
        var comando =
            conexao.prepareStatement(
                "INSERT INTO contas_acesso (email, senha_hash, perfil) VALUES (?, ?, ?)")) {
      comando.setString(1, email);
      comando.setString(2, senhaHash);
      comando.setString(3, perfil);
      comando.executeUpdate();
    }
  }

  /** Leitor e credenciais são gravados juntos: uma falha desfaz as duas inserções. */
  public void cadastrarLeitor(String nome, String email, String senhaHash) throws SQLException {
    try (var conexao = obterConexao()) {
      conexao.setAutoCommit(false);
      try {
        try (var leitor =
            conexao.prepareStatement("INSERT INTO usuarios (nome, email) VALUES (?, ?)")) {
          leitor.setString(1, nome);
          leitor.setString(2, email);
          leitor.executeUpdate();
        }
        try (var conta =
            conexao.prepareStatement(
                "INSERT INTO contas_acesso (email, senha_hash, perfil) VALUES (?, ?,"
                    + " 'CONSULTA')")) {
          conta.setString(1, email);
          conta.setString(2, senhaHash);
          conta.executeUpdate();
        }
        conexao.commit();
      } catch (SQLException e) {
        conexao.rollback();
        throw e;
      }
    }
  }
}
