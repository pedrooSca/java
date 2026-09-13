package com.livraria.controller;

import com.livraria.dao.RecomendacaoDAO;
import com.livraria.dao.UsuarioDAO;
import com.livraria.model.LivroDetalhadoDTO;

import java.sql.SQLException;
import java.util.List;

/**
 * Controller para orquestração de recomendações personalizadas por usuário.
 */
public class RecomendacaoController {

    private final RecomendacaoDAO recomendacaoDAO;
    private final UsuarioDAO usuarioDAO;

    public RecomendacaoController() {
        this.recomendacaoDAO = new RecomendacaoDAO();
        this.usuarioDAO = new UsuarioDAO();
    }

    public RecomendacaoController(RecomendacaoDAO recomendacaoDAO, UsuarioDAO usuarioDAO) {
        this.recomendacaoDAO = recomendacaoDAO;
        this.usuarioDAO = usuarioDAO;
    }

    public List<LivroDetalhadoDTO> obterRecomendacoes(int usuarioId) throws SQLException, IllegalArgumentException {
        if (usuarioDAO.buscarPorId(usuarioId).isEmpty()) {
            throw new IllegalArgumentException("Usuário com ID " + usuarioId + " não foi encontrado no sistema.");
        }
        return recomendacaoDAO.obterRecomendacoesPorUsuario(usuarioId);
    }
}
