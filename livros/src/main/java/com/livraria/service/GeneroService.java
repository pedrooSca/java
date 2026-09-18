package com.livraria.service;

import com.livraria.dao.GeneroDAO;
import com.livraria.model.Genero;

import java.sql.SQLException;
import java.util.List;

/** Regras de negocio e consultas de generos. */
public class GeneroService {

    private final GeneroDAO generoDAO;

    public GeneroService() {
        this.generoDAO = new GeneroDAO();
    }

    public List<Genero> listar() throws SQLException {
        return generoDAO.listarTodos();
    }

    public Genero buscarObrigatorio(int id) throws SQLException {
        return generoDAO.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Gênero com ID " + id + " não existe."));
    }
}
