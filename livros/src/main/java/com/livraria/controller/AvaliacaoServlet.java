package com.livraria.controller;

import com.livraria.model.LivroDetalhadoDTO;
import com.livraria.service.AvaliacaoService;
import com.livraria.service.LivroService;
import com.livraria.service.UsuarioService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name = "AvaliacaoServlet", urlPatterns = "/avaliacoes")
public class AvaliacaoServlet extends BaseServlet {

    private final LivroService livroService = new LivroService();
    private final UsuarioService usuarioService = new UsuarioService();
    private final AvaliacaoService avaliacaoService = new AvaliacaoService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            List<LivroDetalhadoDTO> livros = livroService.listarDetalhados();
            Integer livroId = inteiroOpcional(request, "livroId");
            request.setAttribute("dbOnline", true);
            request.setAttribute("livros", livros);
            request.setAttribute("usuarios", usuarioService.listar());
            request.setAttribute("livroSelecionadoId", livroId);
            request.setAttribute("avaliacoes", livroId == null
                    ? avaliacaoService.listarRecentes(50)
                    : avaliacaoService.listarPorLivro(livroId));
        } catch (SQLException | IllegalArgumentException e) {
            request.setAttribute("dbOnline", false);
        }
        request.setAttribute("avaliacoesLoaded", true);
        encaminhar(request, response, "/avaliar.jsp");
    }
}
