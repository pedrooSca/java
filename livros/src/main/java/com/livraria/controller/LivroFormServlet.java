package com.livraria.controller;

import com.livraria.service.GeneroService;
import com.livraria.service.LivroService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "LivroFormServlet", urlPatterns = {"/novo-livro", "/editar-livro"})
public class LivroFormServlet extends BaseServlet {

    private final LivroService livroService = new LivroService();
    private final GeneroService generoService = new GeneroService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setAttribute("generos", generoService.listar());
            request.setAttribute("dbOnline", true);
            if (request.getServletPath().equals("/editar-livro")) {
                int id = inteiroObrigatorio(request, "id");
                request.setAttribute("livro", livroService.buscarObrigatorio(id));
            }
        } catch (SQLException | IllegalArgumentException e) {
            request.setAttribute("dbOnline", false);
            request.setAttribute("errorMessage", e.getMessage());
        }
        request.setAttribute("livroFormLoaded", true);
        String view = request.getServletPath().equals("/editar-livro")
                ? "/editar-livro.jsp" : "/novo-livro.jsp";
        encaminhar(request, response, view);
    }
}
