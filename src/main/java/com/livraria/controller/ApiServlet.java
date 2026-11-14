package com.livraria.controller;

import com.fasterxml.jackson.databind.*;
import com.livraria.model.Livro;
import com.livraria.service.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

/** HTTP/JSON somente: todas as operações passam pelos Services existentes. */
@WebServlet(urlPatterns = "/api/*")
public class ApiServlet extends JsonServlet {
  private final LivroService livros;
  private final UsuarioService usuarios;
  private final GeneroService generos;
  private final AvaliacaoService avaliacoes;
  private final RecomendacaoService recomendacoes;
  private final LogAuditoriaService auditoria;
  private final ResumoService resumo;

  public ApiServlet() {
    this(new LivroService());
  }

  public ApiServlet(LivroService livros) {
    this(
        livros,
        new UsuarioService(),
        new GeneroService(),
        new AvaliacaoService(),
        new RecomendacaoService(),
        new LogAuditoriaService(),
        new ResumoService());
  }

  public ApiServlet(
      LivroService livros,
      UsuarioService usuarios,
      GeneroService generos,
      AvaliacaoService avaliacoes,
      RecomendacaoService recomendacoes,
      LogAuditoriaService auditoria,
      ResumoService resumo) {
    this.livros = livros;
    this.usuarios = usuarios;
    this.generos = generos;
    this.avaliacoes = avaliacoes;
    this.recomendacoes = recomendacoes;
    this.auditoria = auditoria;
    this.resumo = resumo;
  }

  @Override
  protected void route(HttpServletRequest req, HttpServletResponse res)
      throws SQLException, IOException {
    String path = req.getPathInfo() == null ? "" : req.getPathInfo();
    String resource = path.split("/", -1).length > 1 ? path.split("/", -1)[1] : "";
    switch (resource) {
      case "livros" -> livros(req, res, path);
      case "generos" -> generos(req, res, path);
      case "usuarios" -> usuarios(req, res, path);
      case "avaliacoes" -> avaliacoes(req, res, path);
      case "medias", "auditoria", "resumo" -> consultas(req, res, path);
      default -> throw new HttpError(404, "Endpoint não encontrado.");
    }
  }

  private void livros(HttpServletRequest req, HttpServletResponse res, String path)
      throws SQLException, IOException {
    String method = req.getMethod();
    if (path.equals("/livros")) {
      allow(req, res, "GET, POST, OPTIONS");
      if (method.equals("OPTIONS")) return;
      if (method.equals("GET")) {
        send(res, 200, livros.pesquisar(req.getParameter("q"), queryId(req, "generoId")));
        return;
      }
      JsonNode dados = body(req);
      Livro l =
          livros.cadastrar(
              text(dados, "titulo"),
              text(dados, "autor"),
              text(dados, "isbn"),
              integer(dados, "anoPublicacao", false),
              integer(dados, "generoId", true));
      res.setHeader("Location", req.getContextPath() + "/api/livros/" + l.getId());
      send(res, 201, l);
      return;
    }
    if (path.matches("/livros/[^/]+")) {
      allow(req, res, "GET, PUT, DELETE, OPTIONS");
      if (method.equals("OPTIONS")) return;
      int id = id(path.substring(8));
      if (method.equals("GET")) {
        send(res, 200, livros.buscarObrigatorio(id));
        return;
      }
      if (method.equals("DELETE")) {
        livros.excluir(id);
        res.setStatus(204);
        return;
      }
      JsonNode dados = body(req);
      livros.atualizar(
          id,
          text(dados, "titulo"),
          text(dados, "autor"),
          text(dados, "isbn"),
          integer(dados, "anoPublicacao", false),
          integer(dados, "generoId", true));
      send(res, 200, livros.buscarObrigatorio(id));
      return;
    }
    throw new HttpError(404, "Endpoint não encontrado.");
  }

  private void generos(HttpServletRequest req, HttpServletResponse res, String path)
      throws SQLException, IOException {
    String method = req.getMethod();
    if (!path.equals("/generos")) throw new HttpError(404, "Endpoint não encontrado.");
    allow(req, res, "GET, OPTIONS");
    if (!method.equals("OPTIONS")) send(res, 200, generos.listar());
  }

  private void usuarios(HttpServletRequest req, HttpServletResponse res, String path)
      throws SQLException, IOException {
    String method = req.getMethod();
    if (path.equals("/usuarios")) {
      allow(req, res, "GET, POST, OPTIONS");
      if (method.equals("OPTIONS")) return;
      if (method.equals("GET")) {
        send(res, 200, usuarios.listar());
        return;
      }
      JsonNode dados = body(req);
      var u = usuarios.cadastrar(text(dados, "nome"), text(dados, "email"));
      res.setHeader("Location", req.getContextPath() + "/api/usuarios/" + u.getId());
      send(res, 201, u);
      return;
    }
    if (path.matches("/usuarios/[^/]+")) {
      allow(req, res, "GET, OPTIONS");
      if (!method.equals("OPTIONS"))
        send(
            res,
            200,
            usuarios
                .buscarPorId(id(path.split("/")[2]))
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado.")));
      return;
    }
    if (path.matches("/usuarios/[^/]+/(preferencias|recomendacoes)")) {
      String[] parts = path.split("/");
      int u = id(parts[2]);
      allow(req, res, "GET, OPTIONS");
      if (!method.equals("OPTIONS"))
        send(
            res,
            200,
            parts[3].equals("preferencias")
                ? usuarios.listarPreferencias(u)
                : recomendacoes.obterPorUsuario(u));
      return;
    }
    if (path.matches("/usuarios/[^/]+/preferencias/[^/]+")) {
      allow(req, res, "PUT, DELETE, OPTIONS");
      if (method.equals("OPTIONS")) return;
      String[] parts = path.split("/");
      int u = id(parts[2]), g = id(parts[4]);
      if (method.equals("PUT")) usuarios.adicionarPreferencia(u, g);
      else usuarios.removerPreferencia(u, g);
      res.setStatus(204);
      return;
    }
    throw new HttpError(404, "Endpoint não encontrado.");
  }

  private void avaliacoes(HttpServletRequest req, HttpServletResponse res, String path)
      throws SQLException, IOException {
    String method = req.getMethod();
    if (path.equals("/avaliacoes")) {
      allow(req, res, "GET, POST, OPTIONS");
      if (method.equals("OPTIONS")) return;
      if (method.equals("GET")) {
        Integer l = queryId(req, "livroId");
        if (l != null) livros.buscarObrigatorio(l);
        send(res, 200, l == null ? avaliacoes.listarRecentes(50) : avaliacoes.listarPorLivro(l));
        return;
      }
      JsonNode dados = body(req);
      send(
          res,
          201,
          avaliacoes.cadastrar(
              integer(dados, "usuarioId", true),
              integer(dados, "livroId", true),
              integer(dados, "nota", true),
              text(dados, "comentario")));
      return;
    }
    throw new HttpError(404, "Endpoint não encontrado.");
  }

  private void consultas(HttpServletRequest req, HttpServletResponse res, String path)
      throws SQLException, IOException {
    String method = req.getMethod();
    if (path.equals("/medias")) {
      allow(req, res, "GET, OPTIONS");
      if (!method.equals("OPTIONS")) send(res, 200, avaliacoes.obterMediasPorLivros());
      return;
    }
    if (path.equals("/auditoria")) {
      allow(req, res, "GET, OPTIONS");
      if (!method.equals("OPTIONS")) send(res, 200, auditoria.listar());
      return;
    }
    if (path.equals("/resumo")) {
      allow(req, res, "GET, OPTIONS");
      if (!method.equals("OPTIONS")) send(res, 200, resumo.obter());
      return;
    }
    throw new HttpError(404, "Endpoint não encontrado.");
  }
}
