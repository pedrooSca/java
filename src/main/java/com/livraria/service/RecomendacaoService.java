package com.livraria.service;

import com.livraria.dao.RecomendacaoDAO;
import com.livraria.dao.UsuarioDAO;
import com.livraria.model.LivroDetalhadoDTO;
import java.sql.SQLException;
import java.util.List;

/** Regras de negocio para recomendacoes personalizadas. */
public class RecomendacaoService {

  private final RecomendacaoDAO recomendacaoDAO;
  private final UsuarioDAO usuarioDAO;

  public RecomendacaoService() {
    this(new RecomendacaoDAO(), new UsuarioDAO());
  }

  public RecomendacaoService(RecomendacaoDAO recomendacaoDAO, UsuarioDAO usuarioDAO) {
    this.recomendacaoDAO = recomendacaoDAO;
    this.usuarioDAO = usuarioDAO;
  }

  public List<LivroDetalhadoDTO> obterPorUsuario(int usuarioId) throws SQLException {
    if (usuarioDAO.buscarPorId(usuarioId).isEmpty()) {
      throw new RecursoNaoEncontradoException(
          "Usuário com ID " + usuarioId + " não foi encontrado no sistema.");
    }
    return recomendacaoDAO.obterRecomendacoesPorUsuario(usuarioId);
  }
}
