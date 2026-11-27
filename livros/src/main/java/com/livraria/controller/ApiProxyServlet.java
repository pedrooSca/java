package com.livraria.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.livraria.dao.ApiDAO;
import com.livraria.model.Catalogo;
import com.livraria.model.RequisicaoApi;
import com.livraria.model.RespostaApi;
import com.livraria.service.DadosService;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

/** Controller das requisições de dados feitas pelas telas. */
@WebServlet(urlPatterns = "/api/*", loadOnStartup = 1)
public class ApiProxyServlet extends HttpServlet {
  private static final int LIMITE_CORPO = 256 * 1024;
  private final ObjectMapper json = new ObjectMapper();
  private DadosService dados;

  @Override
  public void init() throws ServletException {
    String apiUrl = getInitParameter("apiUrl");
    if (apiUrl == null) apiUrl = System.getenv("API_URL");
    if (apiUrl == null || apiUrl.isBlank()) apiUrl = "http://localhost:8081";
    dados = new DadosService(new ApiDAO(apiUrl));
  }

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse res) throws IOException {
    // Usa a URI original para preservar os caracteres escapados do caminho.
    String caminho = req.getRequestURI().substring(req.getContextPath().length());
    byte[] corpo = req.getInputStream().readNBytes(LIMITE_CORPO + 1);
    if (corpo.length > LIMITE_CORPO) {
      res.setStatus(413);
      res.setContentType("application/json;charset=UTF-8");
      res.getWriter().write("{\"status\":413,\"erro\":\"Requisição muito grande.\"}");
      return;
    }

    Map<String, String> cabecalhos = new HashMap<>();
    for (String nome : new String[] {"Content-Type", "Accept", "Authorization"}) {
      String valor = req.getHeader(nome);
      if (valor != null) cabecalhos.put(nome, valor);
    }
    res.setHeader("Cache-Control", "no-store");
    if (req.getMethod().equals("GET") && caminho.equals("/api/catalogo")) {
      consultarCatalogo(req, res, cabecalhos);
      return;
    }

    var requisicao =
        new RequisicaoApi(req.getMethod(), caminho, req.getQueryString(), cabecalhos, corpo);
    RespostaApi resposta;
    try {
      resposta = dados.executar(requisicao);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      unavailable(res);
      return;
    } catch (IOException e) {
      log("Não foi possível acessar a API", e);
      unavailable(res);
      return;
    }
    escreverResposta(req, res, resposta);
  }

  private void consultarCatalogo(
      HttpServletRequest req, HttpServletResponse res, Map<String, String> cabecalhos)
      throws IOException {
    Catalogo catalogo;
    try {
      catalogo = dados.consultarCatalogo(req.getQueryString(), cabecalhos);
    } catch (DadosService.ErroDaApi e) {
      escreverResposta(req, res, e.resposta());
      return;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      unavailable(res);
      return;
    } catch (IOException e) {
      log("Não foi possível montar o catálogo", e);
      unavailable(res);
      return;
    }
    res.setContentType("application/json;charset=UTF-8");
    json.writeValue(res.getWriter(), catalogo);
  }

  private void escreverResposta(
      HttpServletRequest req, HttpServletResponse res, RespostaApi resposta) throws IOException {
    res.setStatus(resposta.status());
    for (String header : new String[] {"Content-Type", "Allow", "Location", "WWW-Authenticate"}) {
      resposta.cabecalhos().firstValue(header).ifPresent(value ->
          res.setHeader(
              header,
              header.equals("Location") && value.startsWith("/api/")
                  ? req.getContextPath() + value
                  : value));
    }
    if (!req.getMethod().equals("HEAD") && resposta.status() != 204) {
      res.getOutputStream().write(resposta.corpo());
    }
  }

  private void unavailable(HttpServletResponse res) throws IOException {
    res.setStatus(502);
    res.setContentType("application/json;charset=UTF-8");
    res.getWriter()
        .write("{\"erro\":\"API indisponível. Tente novamente mais tarde.\",\"status\":502}");
  }
}
