package com.livraria.controller;

import com.livraria.config.ConnectionFactory;
import com.livraria.dao.AvaliacaoDAO;
import com.livraria.dao.GeneroDAO;
import com.livraria.model.LivroDetalhadoDTO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

/**
 * Entrada web do módulo de livros no padrão MVC.
 * GET consulta o catálogo; POST executa as alterações do CRUD.
 */
@WebServlet(name = "LivroServlet", urlPatterns = "/livros")
public class LivroServlet extends HttpServlet {

    private final LivroController livroController = new LivroController();
    private final GeneroDAO generoDAO = new GeneroDAO();
    private final AvaliacaoDAO avaliacaoDAO = new AvaliacaoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String termo = valorOuVazio(request.getParameter("q"));
        Integer generoId = inteiroOpcional(request.getParameter("generoId"));

        try {
            List<LivroDetalhadoDTO> livros = termo.isEmpty() && generoId == null
                    ? livroController.listarLivrosDetalhados()
                    : livroController.pesquisarLivros(termo, generoId);

            request.setAttribute("mvcLoaded", true);
            request.setAttribute("dbOnline", true);
            request.setAttribute("q", termo);
            request.setAttribute("generoFiltroId", generoId);
            request.setAttribute("livros", livros);
            request.setAttribute("generos", generoDAO.listarTodos());
            request.setAttribute("medias", avaliacaoDAO.obterMediasPorLivros());
        } catch (SQLException e) {
            request.setAttribute("mvcLoaded", true);
            request.setAttribute("dbOnline", false);
            request.setAttribute("errorMessage", "Não foi possível carregar o catálogo agora.");
        }

        request.getRequestDispatcher("/livros.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String acao = request.getParameter("acao");

        try {
            if ("cadastrar_livro".equals(acao)) {
                livroController.cadastrarLivro(
                        request.getParameter("titulo"),
                        request.getParameter("autor"),
                        request.getParameter("isbn"),
                        inteiroOpcional(request.getParameter("anoPublicacao")),
                        inteiroObrigatorio(request.getParameter("generoId")));
                redirecionar(response, "livro_criado", null);
                return;
            }

            if ("atualizar_livro".equals(acao)) {
                livroController.atualizarLivro(
                        inteiroObrigatorio(request.getParameter("id")),
                        request.getParameter("titulo"),
                        request.getParameter("autor"),
                        request.getParameter("isbn"),
                        inteiroOpcional(request.getParameter("anoPublicacao")),
                        inteiroObrigatorio(request.getParameter("generoId")));
                redirecionar(response, "livro_atualizado", null);
                return;
            }

            if ("excluir_livro".equals(acao)) {
                livroController.excluirLivro(inteiroObrigatorio(request.getParameter("id")));
                redirecionar(response, "livro_excluido", null);
                return;
            }

            redirecionar(response, null, "Ação de livro não reconhecida.");
        } catch (IllegalArgumentException | SQLException e) {
            redirecionar(response, null, e.getMessage());
        }
    }

    private void redirecionar(HttpServletResponse response, String mensagem, String erro)
            throws IOException {
        StringBuilder destino = new StringBuilder(getServletContext().getContextPath()).append("/livros");
        if (mensagem != null) {
            destino.append("?msg=").append(encode(mensagem));
        } else if (erro != null) {
            destino.append("?erro=").append(encode(erro));
        }
        response.sendRedirect(destino.toString());
    }

    private static String valorOuVazio(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private static Integer inteiroOpcional(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }
        return Integer.valueOf(valor.trim());
    }

    private static int inteiroObrigatorio(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException("Um identificador obrigatório não foi informado.");
        }
        return Integer.parseInt(valor.trim());
    }

    private static String encode(String valor) {
        return URLEncoder.encode(valor == null ? "" : valor, StandardCharsets.UTF_8);
    }
}
