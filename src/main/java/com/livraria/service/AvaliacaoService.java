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
    this(new AvaliacaoDAO(), new UsuarioDAO(), new LivroDAO());
  }

  public AvaliacaoService(AvaliacaoDAO avaliacaoDAO, UsuarioDAO usuarioDAO, LivroDAO livroDAO) {
    this.avaliacaoDAO = avaliacaoDAO;
    this.usuarioDAO = usuarioDAO;
    this.livroDAO = livroDAO;
  }

  public Avaliacao cadastrar(int usuarioId, int livroId, int nota, String comentario)
      throws SQLException {
    if (nota < 1 || nota > 5) throw new IllegalArgumentException("A nota deve ser entre 1 e 5.");
    if (comentario != null && comentario.length() > 10000)
      throw new IllegalArgumentException("Comentário deve ter até 10000 caracteres.");
    if (usuarioDAO.buscarPorId(usuarioId).isEmpty()) {
      throw new RecursoNaoEncontradoException("Usuário não encontrado.");
    }
    if (livroDAO.buscarPorId(livroId).isEmpty()) {
      throw new RecursoNaoEncontradoException("Livro não encontrado.");
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
