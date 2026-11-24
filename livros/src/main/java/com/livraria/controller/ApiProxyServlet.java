package com.livraria.controller;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

/** Encaminha as chamadas das telas para a API, sem acessar o banco. */
@WebServlet(urlPatterns = "/api/*", loadOnStartup = 1)
public class ApiProxyServlet extends HttpServlet {
  private final HttpClient client =
      HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
  private String apiUrl;

  @Override
  public void init() throws ServletException {
    apiUrl = getInitParameter("apiUrl");
    if (apiUrl == null) apiUrl = System.getenv("API_URL");
    if (apiUrl == null || apiUrl.isBlank()) apiUrl = "http://localhost:8081";
    apiUrl = apiUrl.replaceAll("/+$", "");
  }

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse res) throws IOException {
    // Usa a URI original para preservar os caracteres escapados do caminho.
    String path = req.getRequestURI().substring(req.getContextPath().length());
    String query = req.getQueryString();
    var builder =
        HttpRequest.newBuilder(URI.create(apiUrl + path + (query == null ? "" : "?" + query)))
            .timeout(Duration.ofSeconds(15));
    for (String header : new String[] {"Content-Type", "Accept", "Authorization"}) {
      String value = req.getHeader(header);
      if (value != null) builder.header(header, value);
    }
    byte[] corpo = req.getInputStream().readNBytes(262145);
    if (corpo.length > 262144) {
      res.setStatus(413);
      res.setContentType("application/json;charset=UTF-8");
      res.getWriter().write("{\"status\":413,\"erro\":\"Requisição muito grande.\"}");
      return;
    }
    builder.method(req.getMethod(), HttpRequest.BodyPublishers.ofByteArray(corpo));
    res.setHeader("Cache-Control", "no-store");
    try {
      var upstream = client.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray());
      res.setStatus(upstream.statusCode());
      for (String header : new String[] {"Content-Type", "Allow", "Location", "WWW-Authenticate"}) {
        upstream
            .headers()
            .firstValue(header)
            .ifPresent(value ->
                res.setHeader(
                    header,
                    header.equals("Location") && value.startsWith("/api/")
                        ? req.getContextPath() + value
                        : value));
      }
      if (!req.getMethod().equals("HEAD") && upstream.statusCode() != 204) {
        res.getOutputStream().write(upstream.body());
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      unavailable(res);
    } catch (IOException e) {
      log("Não foi possível acessar a API", e);
      unavailable(res);
    }
  }

  private void unavailable(HttpServletResponse res) throws IOException {
    res.setStatus(502);
    res.setContentType("application/json;charset=UTF-8");
    res.getWriter()
        .write("{\"erro\":\"API indisponível. Tente novamente mais tarde.\",\"status\":502}");
  }
}
