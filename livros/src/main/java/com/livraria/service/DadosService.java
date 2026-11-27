package com.livraria.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.livraria.dao.ApiDAO;
import com.livraria.model.Catalogo;
import com.livraria.model.RequisicaoApi;
import com.livraria.model.RespostaApi;
import java.io.IOException;
import java.util.Map;

/** Coordena as operações da interface que dependem da API. */
public final class DadosService {
  private final ApiDAO api;
  private final ObjectMapper json =
      new ObjectMapper().enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);

  public DadosService(ApiDAO api) {
    this.api = api;
  }

  public RespostaApi executar(RequisicaoApi requisicao) throws IOException, InterruptedException {
    return api.executar(requisicao);
  }

  public Catalogo consultarCatalogo(String query, Map<String, String> cabecalhos)
      throws IOException, InterruptedException, ErroDaApi {
    var livros = consultarJson("/api/livros", query, cabecalhos);
    var medias = consultarJson("/api/medias", null, cabecalhos);
    if (!(livros instanceof ArrayNode lista) || !(medias instanceof ObjectNode porLivro)) {
      throw new IOException("Formato inválido na resposta do catálogo.");
    }
    return new Catalogo(lista, porLivro);
  }

  private JsonNode consultarJson(String caminho, String query, Map<String, String> cabecalhos)
      throws IOException, InterruptedException, ErroDaApi {
    var resposta = api.executar(new RequisicaoApi("GET", caminho, query, cabecalhos, new byte[0]));
    if (resposta.status() != 200) throw new ErroDaApi(resposta);
    return json.readTree(resposta.corpo());
  }

  public static final class ErroDaApi extends Exception {
    private final RespostaApi resposta;

    private ErroDaApi(RespostaApi resposta) {
      this.resposta = resposta;
    }

    public RespostaApi resposta() {
      return resposta;
    }
  }
}
