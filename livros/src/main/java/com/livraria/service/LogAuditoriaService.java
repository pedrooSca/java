package com.livraria.service;

import com.livraria.dao.LogAuditoriaDAO;
import com.livraria.model.LogAlteracaoLivro;

import java.sql.SQLException;
import java.util.List;

/** Consulta os eventos de auditoria registrados pelo banco. */
public class LogAuditoriaService {

    private final LogAuditoriaDAO logAuditoriaDAO = new LogAuditoriaDAO();

    public List<LogAlteracaoLivro> listar() throws SQLException {
        return logAuditoriaDAO.listarLogs();
    }
}
