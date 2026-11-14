package com.livraria.controller;

import com.livraria.service.AutenticacaoService;
import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet(urlPatterns = "/api/auth/*")
public class AutenticacaoServlet extends JsonServlet {
  private AutenticacaoService autenticacao;

  public AutenticacaoServlet() {}

  public AutenticacaoServlet(AutenticacaoService autenticacao) {
    this.autenticacao = autenticacao;
  }

  @Override
  public void init() throws ServletException {
    if (autenticacao == null)
      autenticacao = (AutenticacaoService) getServletContext().getAttribute("autenticacao");
    if (autenticacao == null) throw new ServletException("Autenticação não inicializada.");
  }

  @Override
  protected void route(HttpServletRequest req, HttpServletResponse res)
      throws SQLException, IOException {
    String caminho = req.getPathInfo();
    if (!"/login".equals(caminho) && !"/contas".equals(caminho) && !"/cadastro".equals(caminho)) {
      throw new HttpError(404, "Endpoint não encontrado.");
    }
    allow(req, res, "POST, OPTIONS");
    if (req.getMethod().equals("OPTIONS")) return;
    var dados = body(req);
    if (caminho.equals("/login")) {
      var acesso = autenticacao.entrar(text(dados, "email"), text(dados, "senha"));
      if (acesso.isEmpty()) throw new HttpError(401, "E-mail ou senha inválidos.");
      send(res, 200, acesso.get());
      return;
    }
    if (caminho.equals("/cadastro")) {
      autenticacao.cadastrarLeitor(text(dados, "nome"), text(dados, "email"), text(dados, "senha"));
      send(res, 201, java.util.Map.of("mensagem", "Usuário cadastrado com sucesso."));
      return;
    }
    autenticacao.cadastrar(text(dados, "email"), text(dados, "senha"), text(dados, "perfil"));
    res.setStatus(201);
  }
}
