package com.livraria.controller;

import com.livraria.model.Genero;
import com.livraria.model.Usuario;
import com.livraria.service.GeneroService;
import com.livraria.service.RecomendacaoService;
import com.livraria.service.UsuarioService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name = "RecomendacaoServlet", urlPatterns = "/recomendacoes")
public class RecomendacaoServlet extends BaseServlet {

    private final UsuarioService usuarioService = new UsuarioService();
    private final GeneroService generoService = new GeneroService();
    private final RecomendacaoService recomendacaoService = new RecomendacaoService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            List<Usuario> usuarios = usuarioService.listar();
            List<Genero> generos = generoService.listar();
            Integer usuarioId = inteiroOpcional(request, "usuarioId");
            if (usuarioId == null && !usuarios.isEmpty()) {
                usuarioId = usuarios.get(0).getId();
            }

            Usuario selecionado = null;
            if (usuarioId != null) {
                for (Usuario usuario : usuarios) {
                    if (usuario.getId() == usuarioId) {
                        selecionado = usuario;
                        break;
                    }
                }
            }

            request.setAttribute("dbOnline", true);
            request.setAttribute("usuarios", usuarios);
            request.setAttribute("generos", generos);
            request.setAttribute("usuarioSelecionadoId", usuarioId);
            request.setAttribute("usuarioSelecionado", selecionado);
            request.setAttribute("recomendacoes", usuarioId == null ? null : recomendacaoService.obterPorUsuario(usuarioId));
            request.setAttribute("preferenciasDoUsuario", usuarioId == null ? null : usuarioService.listarPreferencias(usuarioId));
        } catch (SQLException | IllegalArgumentException e) {
            request.setAttribute("dbOnline", false);
        }
        request.setAttribute("recomendacoesLoaded", true);
        encaminhar(request, response, "/recomendacoes.jsp");
    }
}
