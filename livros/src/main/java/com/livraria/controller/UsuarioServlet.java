package com.livraria.controller;

import com.livraria.service.GeneroService;
import com.livraria.service.UsuarioService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "UsuarioServlet", urlPatterns = "/usuarios")
public class UsuarioServlet extends BaseServlet {

    private final UsuarioService usuarioService = new UsuarioService();
    private final GeneroService generoService = new GeneroService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("usuarios", usuarioService.listar());
            request.setAttribute("todosGeneros", generoService.listar());
            request.setAttribute("dbOnline", true);
        } catch (SQLException e) {
            request.setAttribute("dbOnline", false);
        }
        request.setAttribute("usuariosLoaded", true);
        encaminhar(request, response, "/usuarios.jsp");
    }
}
