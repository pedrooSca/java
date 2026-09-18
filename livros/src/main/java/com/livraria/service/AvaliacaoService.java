package com.livraria.service;

import com.livraria.dao.AvaliacaoDAO;
import com.livraria.dao.LivroDAO;
import com.livraria.dao.UsuarioDAO;
import com.livraria.model.Avaliacao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/** Regras de negocio de avaliacoes. */
public class AvaliacaoService {

    private final AvaliacaoDAO avaliacaoDAO;
    private final UsuarioDAO usuarioDAO;
    private final LivroDAO livroDAO;

    public AvaliacaoService() {
        this.avaliacaoDAO = new AvaliacaoDAO();
        this.usuarioDAO = new UsuarioDAO();
        this.livroDAO = new LivroDAO();
    }

    public Avaliacao cadastrar(int usuarioId, int livroId, int nota, String comentario) throws SQLException {
        if (usuarioDAO.buscarPorId(usuarioId).isEmpty()) {
            throw new IllegalArgumentException("Usuário não encontrado.");
        }
        if (livroDAO.buscarPorId(livroId).isEmpty()) {
            throw new IllegalArgumentException("Livro não encontrado.");
        }
        if (nota < 1 || nota > 5) {
            throw new IllegalArgumentException("A nota da avaliação deve ser entre 1 e 5 estrelas.");
        }
        return avaliacaoDAO.inserir(new Avaliacao(usuarioId, livroId, nota, comentario));
    }

    public List<Avaliacao> listarPorLivro(int livroId) throws SQLException {
        return avaliacaoDAO.listarPorLivro(livroId);
    }

    public List<Avaliacao> listarRecentes(int limite) throws SQLException {
        return avaliacaoDAO.listarTodas(limite);
    }

    public Map<Integer, Double> obterMediasPorLivros() throws SQLException {
        return avaliacaoDAO.obterMediasPorLivros();
    }

    public int contarTotal() throws SQLException {
        return avaliacaoDAO.contarTotal();
    }
}
