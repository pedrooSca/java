package com.livraria.controller;

import com.livraria.service.LogAuditoriaService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "AuditoriaServlet", urlPatterns = "/auditoria")
public class AuditoriaServlet extends BaseServlet {

    private final LogAuditoriaService logAuditoriaService = new LogAuditoriaService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("logs", logAuditoriaService.listar());
            request.setAttribute("dbOnline", true);
        } catch (SQLException e) {
            request.setAttribute("dbOnline", false);
        }
        request.setAttribute("auditoriaLoaded", true);
        encaminhar(request, response, "/auditoria.jsp");
    }
}
