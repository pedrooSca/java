package com.livraria.service;

import com.livraria.dao.GeneroDAO;
import com.livraria.dao.UsuarioDAO;
import com.livraria.model.Genero;
import com.livraria.model.Usuario;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/** Regras de negocio de usuarios e preferencias. */
public class UsuarioService {

    private final UsuarioDAO usuarioDAO;
    private final GeneroDAO generoDAO;

    public UsuarioService() {
        this.usuarioDAO = new UsuarioDAO();
        this.generoDAO = new GeneroDAO();
    }

    public List<Usuario> listar() throws SQLException {
        return usuarioDAO.listarTodos();
    }

    public Optional<Usuario> buscarPorId(int id) throws SQLException {
        return usuarioDAO.buscarPorId(id);
    }

    public Usuario cadastrar(String nome, String email) throws SQLException {
        String nomeNormalizado = obrigatorio(nome, "Nome");
        String emailNormalizado = obrigatorio(email, "E-mail");
        validarEmail(emailNormalizado);
        return usuarioDAO.inserir(new Usuario(nomeNormalizado, emailNormalizado));
    }

    public List<Genero> listarPreferencias(int usuarioId) throws SQLException {
        validarUsuario(usuarioId);
        return usuarioDAO.obterGenerosPreferidos(usuarioId);
    }

    public void adicionarPreferencia(int usuarioId, int generoId) throws SQLException {
        validarUsuario(usuarioId);
        validarGenero(generoId);
        usuarioDAO.adicionarPreferencia(usuarioId, generoId);
    }

    public void removerPreferencia(int usuarioId, int generoId) throws SQLException {
        validarUsuario(usuarioId);
        validarGenero(generoId);
        usuarioDAO.removerPreferencia(usuarioId, generoId);
    }

    private void validarUsuario(int id) throws SQLException {
        if (usuarioDAO.buscarPorId(id).isEmpty()) {
            throw new IllegalArgumentException("Usuário com ID " + id + " não foi encontrado no sistema.");
        }
    }

    private void validarGenero(int id) throws SQLException {
        if (generoDAO.buscarPorId(id).isEmpty()) {
            throw new IllegalArgumentException("Gênero com ID " + id + " não existe.");
        }
    }

    private String obrigatorio(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(campo + " é obrigatório.");
        }
        return valor.trim();
    }

    private void validarEmail(String email) {
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Informe um e-mail válido.");
        }
    }
}
