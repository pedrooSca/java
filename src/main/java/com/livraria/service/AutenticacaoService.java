package com.livraria.service;

import com.livraria.dao.ContaAcessoDAO;
import com.livraria.security.*;
import java.sql.SQLException;
import java.util.*;

public class AutenticacaoService {
  private final ContaAcessoDAO contas;
  private final JwtService jwt;
  private final UsuarioService usuarios;
  // Executa a mesma derivação quando o e-mail não existe, sem revelar a existência da conta.
  private final String hashInexistente = Senhas.gerarHash(UUID.randomUUID().toString());

  public AutenticacaoService(ContaAcessoDAO contas, JwtService jwt) {
    this(contas, jwt, new UsuarioService());
  }

  public AutenticacaoService(ContaAcessoDAO contas, JwtService jwt, UsuarioService usuarios) {
    this.contas = contas;
    this.jwt = jwt;
    this.usuarios = usuarios;
  }

  public Optional<Map<String, Object>> entrar(String email, String senha) throws SQLException {
    var conta = contas.buscarPorEmail(normalizarEmail(email));
    boolean correta =
        Senhas.verificar(senha, conta.map(c -> c.senhaHash()).orElse(hashInexistente));
    if (!correta || conta.isEmpty()) return Optional.empty();
    var acesso = conta.get();
    return Optional.of(
        Map.of(
            "token",
            jwt.emitir(acesso.id(), acesso.perfil()),
            "tipo",
            "Bearer",
            "expiraEm",
            1800,
            "perfil",
            acesso.perfil(),
            "email",
            acesso.email()));
  }

  public void cadastrar(String email, String senha, String perfil) throws SQLException {
    if (!Set.of("ADMIN", "CONSULTA").contains(perfil == null ? "" : perfil)) {
      throw new IllegalArgumentException("Perfil deve ser ADMIN ou CONSULTA.");
    }
    contas.cadastrar(normalizarEmail(email), Senhas.gerarHash(senha), perfil);
  }

  /** O cadastro público cria somente um perfil de consulta, nunca administrador. */
  public void cadastrarLeitor(String nome, String email, String senha) throws SQLException {
    String emailNormalizado = normalizarEmail(email);
    var leitor = usuarios.validarCadastro(nome, emailNormalizado);
    contas.cadastrarLeitor(leitor.getNome(), emailNormalizado, Senhas.gerarHash(senha));
  }

  public void criarAdministradorInicial(String email, String senha) throws SQLException {
    String normalizado = normalizarEmail(email);
    if (contas.buscarPorEmail(normalizado).isEmpty()) cadastrar(normalizado, senha, "ADMIN");
  }

  private String normalizarEmail(String email) {
    if (email == null || email.length() > 100 || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
      throw new IllegalArgumentException("Informe um e-mail válido.");
    }
    return email.trim().toLowerCase(Locale.ROOT);
  }
}
