package com.livraria.controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

/** Rotas das telas. Os dados são obtidos pelo JavaScript através do proxy. */
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
      "/auditoria"
    })
public class PaginaServlet extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    String page =
        switch (req.getServletPath()) {
          case "/home" -> "index";
          case "/avaliacoes" -> "avaliar";
          default -> req.getServletPath().substring(1);
        };
    req.getRequestDispatcher("/" + page + ".jsp").forward(req, res);
  }
}
