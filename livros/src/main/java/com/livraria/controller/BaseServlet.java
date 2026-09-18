package com.livraria.controller;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

/** Helpers compartilhados pelos controllers web. */
public abstract class BaseServlet extends HttpServlet {

    protected String parametro(HttpServletRequest request, String nome) {
        return request.getParameter(nome);
    }

    protected Integer inteiroOpcional(HttpServletRequest request, String nome) {
        String valor = parametro(request, nome);
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }
        return Integer.valueOf(valor.trim());
    }

    protected int inteiroObrigatorio(HttpServletRequest request, String nome) {
        Integer valor = inteiroOpcional(request, nome);
        if (valor == null) {
            throw new IllegalArgumentException("O parâmetro " + nome + " é obrigatório.");
        }
        return valor;
    }

    protected String valorOuVazio(HttpServletRequest request, String nome) {
        String valor = parametro(request, nome);
        return valor == null ? "" : valor.trim();
    }

    protected void encaminhar(HttpServletRequest request, HttpServletResponse response, String view)
            throws ServletException, IOException {
        request.getRequestDispatcher(view).forward(request, response);
    }

    protected void redirecionar(HttpServletRequest request, HttpServletResponse response, String caminho)
            throws IOException {
        response.sendRedirect(request.getContextPath() + caminho);
    }
}
