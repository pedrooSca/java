package com.livraria.controller;

import com.fasterxml.jackson.databind.*;
import com.livraria.service.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import javax.servlet.http.*;

/** Contrato JSON e tratamento uniforme dos erros HTTP. */
public abstract class JsonServlet extends HttpServlet {
  protected final ObjectMapper json =
      new ObjectMapper()
          .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
          .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse res) throws IOException {
    req.setCharacterEncoding("UTF-8");
    res.setCharacterEncoding("UTF-8");
    res.setContentType("application/json");
    res.setHeader("Cache-Control", "no-store");
    try {
      route(req, res);
    } catch (HttpError e) {
      error(res, e.status, e.getMessage());
    } catch (RecursoNaoEncontradoException e) {
      error(res, 404, e.getMessage());
    } catch (IllegalArgumentException e) {
      error(res, 400, e.getMessage());
    } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
      error(res, 400, "JSON inválido. Envie um objeto com os tipos documentados.");
    } catch (SQLException e) {
      log("Falha JDBC na API", e);
      String state = e.getSQLState();
      if (state != null && state.startsWith("23"))
        error(res, 409, "Operação conflita com um registro existente ou relacionado.");
      else
        error(res, 503, "Banco de dados temporariamente indisponível. Tente novamente mais tarde.");
    } catch (Exception e) {
      log("Falha inesperada na API", e);
      error(res, 500, "Não foi possível concluir a operação.");
    }
  }

  protected abstract void route(HttpServletRequest req, HttpServletResponse res)
      throws SQLException, IOException;

  protected void allow(HttpServletRequest req, HttpServletResponse res, String methods) {
    res.setHeader("Allow", methods);
    if (!Arrays.asList(methods.split(", ")).contains(req.getMethod()))
      throw new HttpError(405, "Método HTTP não permitido neste endpoint.");
    if (req.getMethod().equals("OPTIONS")) res.setStatus(204);
  }

  protected JsonNode body(HttpServletRequest req) throws IOException {
    String type = req.getContentType();
    if (type == null || !type.split(";")[0].trim().equalsIgnoreCase("application/json"))
      throw new HttpError(415, "Envie Content-Type: application/json.");
    // Limita o corpo antes de fazer o parse, inclusive para transferências sem Content-Length.
    char[] chars = new char[65537];
    int count = 0, n;
    var reader = req.getReader();
    while (count < chars.length && (n = reader.read(chars, count, chars.length - count)) != -1)
      count += n;
    if (count > 65536)
      throw new HttpError(413, "Corpo da requisição excede o limite de 64 Ki caracteres.");
    JsonNode b = json.readTree(new String(chars, 0, count));
    if (b == null || !b.isObject()) throw new HttpError(400, "Envie um objeto JSON.");
    return b;
  }

  protected String text(JsonNode b, String field) {
    JsonNode n = b.get(field);
    if (n == null || n.isNull()) return null;
    if (!n.isTextual()) throw new HttpError(400, field + " deve ser texto.");
    return n.textValue();
  }

  protected Integer integer(JsonNode b, String field, boolean required) {
    JsonNode n = b.get(field);
    if (n == null || n.isNull()) {
      if (required) throw new HttpError(400, field + " é obrigatório.");
      return null;
    }
    if (!n.isIntegralNumber() || !n.canConvertToInt())
      throw new HttpError(400, field + " deve ser um número inteiro válido.");
    int v = n.intValue();
    if (required && v <= 0) throw new HttpError(400, field + " deve ser positivo.");
    return v;
  }

  protected int id(String value) {
    try {
      if (!value.matches("[0-9]+")) throw new NumberFormatException();
      int id = Integer.parseInt(value);
      if (id <= 0) throw new NumberFormatException();
      return id;
    } catch (NumberFormatException e) {
      throw new HttpError(400, "Identificador deve ser um inteiro positivo.");
    }
  }

  protected Integer queryId(HttpServletRequest req, String name) {
    String v = req.getParameter(name);
    return v == null || v.isBlank() ? null : id(v);
  }

  protected void send(HttpServletResponse res, int status, Object value) throws IOException {
    res.setStatus(status);
    json.writeValue(res.getWriter(), value);
  }

  protected void error(HttpServletResponse res, int status, String message) throws IOException {
    if (status == 401) res.setHeader("WWW-Authenticate", "Bearer");
    send(res, status, Map.of("status", status, "erro", message));
  }

  protected static class HttpError extends RuntimeException {
    final int status;

    HttpError(int status, String message) {
      super(message);
      this.status = status;
    }
  }
}
