package com.livraria.view;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

/** Representação JSON dos resultados e erros da API. */
public final class RespostaJson {
  private static final ObjectWriter JSON =
      new ObjectMapper().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS).writer();

  private RespostaJson() {}

  public static void preparar(HttpServletResponse resposta) {
    resposta.setCharacterEncoding("UTF-8");
    resposta.setContentType("application/json");
    resposta.setHeader("Cache-Control", "no-store");
  }

  public static void escrever(HttpServletResponse resposta, int status, Object dados)
      throws IOException {
    resposta.setStatus(status);
    JSON.writeValue(resposta.getWriter(), dados);
  }
}
