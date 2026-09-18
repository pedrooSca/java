package com.livraria.controller;

import com.livraria.model.Avaliacao;
import com.livraria.model.LivroDetalhadoDTO;
import com.livraria.service.AvaliacaoService;
import com.livraria.service.GeneroService;
import com.livraria.service.LivroService;
import com.livraria.service.RecomendacaoService;
import com.livraria.service.UsuarioService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@WebServlet(name = "HomeServlet", urlPatterns = "/home")
public class HomeServlet extends BaseServlet {

    private final LivroService livroService = new LivroService();
    private final RecomendacaoService recomendacaoService = new RecomendacaoService();
    private final AvaliacaoService avaliacaoService = new AvaliacaoService();
    private final GeneroService generoService = new GeneroService();
    private final UsuarioService usuarioService = new UsuarioService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            List<LivroDetalhadoDTO> livros = livroService.listarDetalhados();
            List<LivroDetalhadoDTO> recomendacoes = recomendacaoService.obterPorUsuario(1);
            Map<Integer, Double> medias = avaliacaoService.obterMediasPorLivros();
            List<Avaliacao> avaliacoesRecentes = avaliacaoService.listarRecentes(4);

            request.setAttribute("dbOnline", true);
            request.setAttribute("livros", livros);
            request.setAttribute("recomendacoes", recomendacoes);
            request.setAttribute("medias", medias);
            request.setAttribute("avaliacoesRecentes", avaliacoesRecentes);
            request.setAttribute("totalLivros", livros.size());
            request.setAttribute("totalGeneros", generoService.listar().size());
            request.setAttribute("totalUsuarios", usuarioService.listar().size());
            request.setAttribute("totalAvaliacoes", avaliacaoService.contarTotal());
        } catch (SQLException | IllegalArgumentException e) {
            request.setAttribute("dbOnline", false);
            request.setAttribute("errorMessage", "Não foi possível carregar o resumo agora.");
        }
        request.setAttribute("homeLoaded", true);
        encaminhar(request, response, "/index.jsp");
    }
}
