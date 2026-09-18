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
        this.recomendacaoDAO = new RecomendacaoDAO();
        this.usuarioDAO = new UsuarioDAO();
    }

    public List<LivroDetalhadoDTO> obterPorUsuario(int usuarioId) throws SQLException {
        if (usuarioDAO.buscarPorId(usuarioId).isEmpty()) {
            throw new IllegalArgumentException("Usuário com ID " + usuarioId + " não foi encontrado no sistema.");
        }
        return recomendacaoDAO.obterRecomendacoesPorUsuario(usuarioId);
    }
}
