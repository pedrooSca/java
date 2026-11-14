package com.livraria.service;

import com.livraria.dao.GeneroDAO;
import com.livraria.dao.LivroDAO;
import com.livraria.model.Livro;
import com.livraria.model.LivroDetalhadoDTO;
import java.sql.SQLException;
import java.time.Year;
import java.util.List;
import java.util.Optional;

/** Regras de negocio do catalogo de livros. */
public class LivroService {

  private final LivroDAO livroDAO;
  private final GeneroDAO generoDAO;

  public LivroService() {
    this(new LivroDAO(), new GeneroDAO());
  }

  public LivroService(LivroDAO livroDAO, GeneroDAO generoDAO) {
    this.livroDAO = livroDAO;
    this.generoDAO = generoDAO;
  }

  public Livro cadastrar(
      String titulo, String autor, String isbn, Integer anoPublicacao, int generoId)
      throws SQLException {
    validarDados(titulo, autor, anoPublicacao);
    validarIsbn(isbn);
    validarGenero(generoId);
    return livroDAO.inserir(
        new Livro(titulo.trim(), autor.trim(), normalizar(isbn), anoPublicacao, generoId));
  }

  public boolean atualizar(
      int id, String titulo, String autor, String isbn, Integer anoPublicacao, int generoId)
      throws SQLException {
    validarDados(titulo, autor, anoPublicacao);
    buscarObrigatorio(id);
    validarIsbn(isbn);
    validarGenero(generoId);
    if (!livroDAO.atualizar(
        new Livro(id, titulo.trim(), autor.trim(), normalizar(isbn), anoPublicacao, generoId)))
      throw new RecursoNaoEncontradoException("Livro não encontrado.");
    return true;
  }

  public boolean excluir(int id) throws SQLException {
    buscarObrigatorio(id);
    if (!livroDAO.excluir(id)) throw new RecursoNaoEncontradoException("Livro não encontrado.");
    return true;
  }

  public Optional<Livro> buscarPorId(int id) throws SQLException {
    return livroDAO.buscarPorId(id);
  }

  public Livro buscarObrigatorio(int id) throws SQLException {
    return livroDAO
        .buscarPorId(id)
        .orElseThrow(
            () -> new RecursoNaoEncontradoException("Livro com ID " + id + " não encontrado."));
  }

  public List<LivroDetalhadoDTO> listarDetalhados() throws SQLException {
    return livroDAO.listarDetalhados();
  }

  public List<LivroDetalhadoDTO> pesquisar(String termo, Integer generoId) throws SQLException {
    return livroDAO.pesquisar(termo, generoId);
  }

  private void validarGenero(int generoId) throws SQLException {
    if (generoDAO.buscarPorId(generoId).isEmpty()) {
      throw new IllegalArgumentException("Gênero com ID " + generoId + " não existe.");
    }
  }

  private void validarIsbn(String isbn) {
    if (isbn != null && isbn.trim().length() > 20)
      throw new IllegalArgumentException("ISBN deve ter até 20 caracteres.");
  }

  private void validarDados(String titulo, String autor, Integer anoPublicacao) {
    if (titulo == null || titulo.trim().isEmpty()) {
      throw new IllegalArgumentException("O título do livro é obrigatório.");
    }
    if (autor == null || autor.trim().isEmpty()) {
      throw new IllegalArgumentException("O autor do livro é obrigatório.");
    }
    if (titulo.trim().length() > 150 || autor.trim().length() > 100) {
      throw new IllegalArgumentException("Título deve ter até 150 caracteres e autor até 100.");
    }
    if (anoPublicacao != null && (anoPublicacao < 0 || anoPublicacao > Year.now().getValue() + 1)) {
      throw new IllegalArgumentException("Ano de publicação inválido (" + anoPublicacao + ").");
    }
  }

  private String normalizar(String valor) {
    if (valor == null) {
      return null;
    }
    String normalizado = valor.trim();
    return normalizado.isEmpty() ? null : normalizado;
  }
}
