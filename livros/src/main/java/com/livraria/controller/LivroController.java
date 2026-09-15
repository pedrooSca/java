package com.livraria.controller;

import com.livraria.dao.GeneroDAO;
import com.livraria.dao.LivroDAO;
import com.livraria.model.Livro;
import com.livraria.model.LivroDetalhadoDTO;

import java.sql.SQLException;
import java.time.Year;
import java.util.List;
import java.util.Optional;

/**
 * Controller responsável pelas regras de negócio e validações para Livros.
 */
public class LivroController {

    private final LivroDAO livroDAO;
    private final GeneroDAO generoDAO;

    public LivroController() {
        this.livroDAO = new LivroDAO();
        this.generoDAO = new GeneroDAO();
    }

    public LivroController(LivroDAO livroDAO, GeneroDAO generoDAO) {
        this.livroDAO = livroDAO;
        this.generoDAO = generoDAO;
    }

    public Livro cadastrarLivro(String titulo, String autor, String isbn, Integer anoPublicacao, int generoId) throws SQLException, IllegalArgumentException {
        validarDadosLivro(titulo, autor, anoPublicacao);

        if (generoDAO.buscarPorId(generoId).isEmpty()) {
            throw new IllegalArgumentException("Gênero com ID " + generoId + " não existe.");
        }

        Livro novoLivro = new Livro(titulo.trim(), autor.trim(), isbn != null ? isbn.trim() : null, anoPublicacao, generoId);
        return livroDAO.inserir(novoLivro);
    }

    public boolean atualizarLivro(int id, String titulo, String autor, String isbn, Integer anoPublicacao, int generoId) throws SQLException, IllegalArgumentException {
        validarDadosLivro(titulo, autor, anoPublicacao);

        if (livroDAO.buscarPorId(id).isEmpty()) {
            throw new IllegalArgumentException("Livro com ID " + id + " não encontrado.");
        }

        if (generoDAO.buscarPorId(generoId).isEmpty()) {
            throw new IllegalArgumentException("Gênero com ID " + generoId + " não existe.");
        }

        Livro livro = new Livro(id, titulo.trim(), autor.trim(), isbn != null ? isbn.trim() : null, anoPublicacao, generoId);
        return livroDAO.atualizar(livro);
    }

    public boolean excluirLivro(int id) throws SQLException {
        if (livroDAO.buscarPorId(id).isEmpty()) {
            throw new IllegalArgumentException("Livro com ID " + id + " não encontrado.");
        }
        return livroDAO.excluir(id);
    }

    public Optional<Livro> buscarPorId(int id) throws SQLException {
        return livroDAO.buscarPorId(id);
    }

    public List<LivroDetalhadoDTO> listarLivrosDetalhados() throws SQLException {
        return livroDAO.listarDetalhados();
    }

    public List<LivroDetalhadoDTO> pesquisarLivros(String termo, Integer generoId) throws SQLException {
        return livroDAO.pesquisar(termo, generoId);
    }

    private void validarDadosLivro(String titulo, String autor, Integer anoPublicacao) {
        if (titulo == null || titulo.trim().isEmpty()) {
            throw new IllegalArgumentException("O título do livro é obrigatório.");
        }
        if (autor == null || autor.trim().isEmpty()) {
            throw new IllegalArgumentException("O autor do livro é obrigatório.");
        }
        if (anoPublicacao != null) {
            int anoAtual = Year.now().getValue() + 1;
            if (anoPublicacao < 0 || anoPublicacao > anoAtual) {
                throw new IllegalArgumentException("Ano de publicação inválido (" + anoPublicacao + ").");
            }
        }
    }
}
