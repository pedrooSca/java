package com.livraria.controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

/** Controller das telas. Encaminha as rotas públicas para as views JSP. */
@WebServlet(
    urlPatterns = {
      "/login",
      "/home",
      "/livros",
      "/novo-livro",
      "/editar-livro",
      "/avaliacoes",
      "/recomendacoes",
      "/usuarios",
      "/auditoria",
      "/login.jsp",
      "/index.jsp",
      "/livros.jsp",
      "/novo-livro.jsp",
      "/editar-livro.jsp",
      "/avaliar.jsp",
      "/recomendacoes.jsp",
      "/usuarios.jsp",
      "/auditoria.jsp"
    })
public class PaginaServlet extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    String rota = req.getServletPath().substring(1);
    String pagina =
        switch (rota) {
          case "home" -> "index.jsp";
          case "avaliacoes" -> "avaliar.jsp";
          default -> rota.endsWith(".jsp") ? rota : rota + ".jsp";
        };
    req.getRequestDispatcher("/WEB-INF/views/" + pagina).forward(req, res);
  }
}
