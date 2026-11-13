package com.livraria.service;

import java.sql.SQLException;
import java.util.*;

/** Agrega os dados da página inicial através dos serviços injetados. */
public class ResumoService {
  private final UsuarioService usuarios;
  private final LivroService livros;
  private final GeneroService generos;
  private final AvaliacaoService avaliacoes;
  private final RecomendacaoService recomendacoes;

  public ResumoService() {
    this(
        new UsuarioService(),
        new LivroService(),
        new GeneroService(),
        new AvaliacaoService(),
        new RecomendacaoService());
  }

  public ResumoService(
      UsuarioService usuarios,
      LivroService livros,
      GeneroService generos,
      AvaliacaoService avaliacoes,
      RecomendacaoService recomendacoes) {
    this.usuarios = usuarios;
    this.livros = livros;
    this.generos = generos;
    this.avaliacoes = avaliacoes;
    this.recomendacoes = recomendacoes;
  }

  public Map<String, Object> obter() throws SQLException {
    var leitores = usuarios.listar();
    Map<String, Object> resumo = new LinkedHashMap<>();
    resumo.put("totalLivros", livros.listarDetalhados().size());
    resumo.put("totalGeneros", generos.listar().size());
    resumo.put("totalUsuarios", leitores.size());
    resumo.put("totalAvaliacoes", avaliacoes.contarTotal());
    resumo.put("avaliacoes", avaliacoes.listarRecentes(4));
    resumo.put("usuario", leitores.isEmpty() ? null : leitores.get(0));
    resumo.put(
        "recomendacoes",
        leitores.isEmpty() ? List.of() : recomendacoes.obterPorUsuario(leitores.get(0).getId()));
    return resumo;
  }
}
