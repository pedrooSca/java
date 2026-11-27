package com.livraria.dao;

import com.livraria.model.RequisicaoApi;
import com.livraria.model.RespostaApi;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** Acessa os dados do módulo API por HTTP. */
public final class ApiDAO {
  private final HttpClient client =
      HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
  private final String apiUrl;

  public ApiDAO(String apiUrl) {
    this.apiUrl = apiUrl.replaceAll("/+$", "");
  }

  public RespostaApi executar(RequisicaoApi requisicao) throws IOException, InterruptedException {
    String destino = apiUrl + requisicao.caminho();
    if (requisicao.query() != null) destino += "?" + requisicao.query();
    var builder =
        HttpRequest.newBuilder(URI.create(destino))
            .timeout(Duration.ofSeconds(15))
            .method(requisicao.metodo(), HttpRequest.BodyPublishers.ofByteArray(requisicao.corpo()));
    requisicao.cabecalhos().forEach(builder::header);
    var resposta = client.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray());
    return new RespostaApi(resposta.statusCode(), resposta.headers(), resposta.body());
  }
}
