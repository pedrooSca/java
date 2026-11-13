package com.livraria.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import java.io.IOException;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.*;

/** Confere a identidade antes do controller e restringe alterações ao administrador. */
@WebFilter(urlPatterns = "/api/*")
public class AutenticacaoFilter implements Filter {
  private JwtService jwt;

  public AutenticacaoFilter() {}

  public AutenticacaoFilter(JwtService jwt) {
    this.jwt = jwt;
  }

  @Override
  public void init(FilterConfig config) throws ServletException {
    if (jwt == null) jwt = (JwtService) config.getServletContext().getAttribute("jwt");
    if (jwt == null) throw new ServletException("JWT não inicializado.");
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    var req = (HttpServletRequest) request;
    var res = (HttpServletResponse) response;
    String caminho = req.getServletPath();
    // Login, cadastro de consulta e descoberta dos métodos são públicos.
    if (req.getMethod().equals("OPTIONS")
        || (caminho.equals("/api/auth")
            && ("/login".equals(req.getPathInfo()) || "/cadastro".equals(req.getPathInfo()))
            && req.getMethod().equals("POST"))) {
      chain.doFilter(req, res);
      return;
    }
    String header = req.getHeader("Authorization");
    if (header == null || !header.startsWith("Bearer ")) {
      negar(res, 401);
      return;
    }
    String perfil;
    try {
      var token = jwt.verificar(header.substring(7));
      perfil = token.getClaim("perfil").asString();
      if (!"ADMIN".equals(perfil) && !"CONSULTA".equals(perfil)) {
        negar(res, 401);
        return;
      }
      req.setAttribute("contaId", token.getSubject());
    } catch (JWTVerificationException | IllegalArgumentException e) {
      negar(res, 401);
      return;
    }
    boolean leitura = req.getMethod().equals("GET") || req.getMethod().equals("HEAD");
    boolean restrito =
        caminho.equals("/api/auth")
            || (caminho.equals("/api") && "/auditoria".equals(req.getPathInfo()));
    if (!perfil.equals("ADMIN") && (!leitura || restrito)) {
      negar(res, 403);
      return;
    }
    chain.doFilter(req, res);
  }

  private void negar(HttpServletResponse res, int status) throws IOException {
    res.setStatus(status);
    res.setContentType("application/json;charset=UTF-8");
    res.setHeader("Cache-Control", "no-store");
    if (status == 401) res.setHeader("WWW-Authenticate", "Bearer");
    String mensagem =
        status == 401
            ? "Autenticação necessária ou token expirado."
            : "Seu perfil não permite esta operação.";
    res.getWriter().write("{\"status\":" + status + ",\"erro\":\"" + mensagem + "\"}");
  }
}
