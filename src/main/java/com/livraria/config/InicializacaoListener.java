package com.livraria.config;

import com.livraria.dao.ContaAcessoDAO;
import com.livraria.security.JwtService;
import com.livraria.service.AutenticacaoService;
import java.nio.charset.StandardCharsets;
import javax.servlet.*;
import javax.servlet.annotation.WebListener;

/** Atualização idempotente: preserva leitores, livros e credenciais existentes. */
@WebListener
public class InicializacaoListener implements ServletContextListener {
  @Override
  public void contextInitialized(ServletContextEvent event) {
    var contexto = event.getServletContext();
    try {
      var jwt = new JwtService(obrigatorio("JWT_SECRET"));
      // Prepara apenas a tabela de acesso e os gêneros ausentes em instalações anteriores.
      try (var entrada = getClass().getClassLoader().getResourceAsStream("atualizar.sql");
          var conexao = ConnectionFactory.getConnection();
          var comando = conexao.createStatement()) {
        if (entrada == null) throw new IllegalStateException("Atualização SQL ausente.");
        String sql = new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
        for (String instrucao : sql.split(";")) {
          if (!instrucao.isBlank()) comando.execute(instrucao);
        }
      }
      var autenticacao = new AutenticacaoService(new ContaAcessoDAO(), jwt);
      autenticacao.criarAdministradorInicial(
          obrigatorio("ADMIN_EMAIL"), obrigatorio("ADMIN_PASSWORD"));
      contexto.setAttribute("jwt", jwt);
      contexto.setAttribute("autenticacao", autenticacao);
    } catch (Exception e) {
      throw new IllegalStateException(
          "Falha ao inicializar a API. Confira o banco e as variáveis de autenticação.", e);
    }
  }

  private String obrigatorio(String nome) {
    String valor = System.getenv(nome);
    if (valor == null || valor.isBlank())
      throw new IllegalArgumentException("Defina " + nome + ".");
    return valor;
  }
}
