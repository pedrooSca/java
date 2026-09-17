package com.livraria.controller;

import com.livraria.dao.AvaliacaoDAO;
import com.livraria.dao.UsuarioDAO;
import com.livraria.model.Avaliacao;
import com.livraria.model.Usuario;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * Controller web para as operacoes que nao pertencem exclusivamente ao catalogo.
 * Mantem as JSPs responsaveis apenas pela apresentacao e usa PRG apos cada POST.
 */
@WebServlet(name = "AcoesServlet", urlPatterns = "/acoes")
public class AcoesServlet extends HttpServlet {

    private final AvaliacaoDAO avaliacaoDAO = new AvaliacaoDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String acao = request.getParameter("acao");

        try {
            if ("avaliar_livro".equals(acao)) {
                int usuarioId = inteiroObrigatorio(request.getParameter("usuarioId"));
                int livroId = inteiroObrigatorio(request.getParameter("livroId"));
                int nota = inteiroObrigatorio(request.getParameter("nota"));
                String comentario = valorOuVazio(request.getParameter("comentario"));

                avaliacaoDAO.inserir(new Avaliacao(usuarioId, livroId, nota, comentario));
                redirecionar(response, "/avaliar.jsp?livroId=" + livroId + "&msg=avaliacao_criada", null);
                return;
            }

            if ("cadastrar_usuario".equals(acao)) {
                String nome = valorObrigatorio(request.getParameter("nome"), "Nome");
                String email = valorObrigatorio(request.getParameter("email"), "E-mail");
                validarEmail(email);

                usuarioDAO.inserir(new Usuario(nome, email));
                redirecionar(response, "/usuarios.jsp?msg=usuario_criado", null);
                return;
            }

            if ("adicionar_preferencia".equals(acao)) {
                int usuarioId = inteiroObrigatorio(request.getParameter("usuarioId"));
                int generoId = inteiroObrigatorio(request.getParameter("generoId"));
                usuarioDAO.adicionarPreferencia(usuarioId, generoId);
                redirecionar(response, "/recomendacoes.jsp?usuarioId=" + usuarioId + "&msg=preferencia_atualizada", null);
                return;
            }

            if ("remover_preferencia".equals(acao)) {
                int usuarioId = inteiroObrigatorio(request.getParameter("usuarioId"));
                int generoId = inteiroObrigatorio(request.getParameter("generoId"));
                usuarioDAO.removerPreferencia(usuarioId, generoId);
                redirecionar(response, "/recomendacoes.jsp?usuarioId=" + usuarioId + "&msg=preferencia_atualizada", null);
                return;
            }

            redirecionar(response, "/index.jsp", "Ação não reconhecida.");
        } catch (IllegalArgumentException | SQLException e) {
            redirecionar(response, origemSegura(request), e.getMessage());
        }
    }

    private void redirecionar(HttpServletResponse response, String caminho, String erro)
            throws IOException {
        String destino = caminho == null ? "/index.jsp" : caminho;
        if (erro != null && !erro.isEmpty()) {
            destino += destino.contains("?") ? "&erro=" : "?erro=";
            destino += encode(erro);
        }
        response.sendRedirect(response.encodeRedirectURL(
                getServletContext().getContextPath() + destino));
    }

    private static String origemSegura(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer == null || referer.isEmpty()) {
            return "/index.jsp";
        }
        int queryStart = referer.indexOf('?');
        int hostEnd = referer.indexOf('/', referer.indexOf("//") + 2);
        if (hostEnd < 0) {
            return "/index.jsp";
        }
        String caminho = queryStart >= 0 ? referer.substring(hostEnd, queryStart) : referer.substring(hostEnd);
        return caminho.startsWith("/") ? caminho : "/index.jsp";
    }

    private static String valorOuVazio(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private static String valorObrigatorio(String valor, String campo) {
        String normalizado = valorOuVazio(valor);
        if (normalizado.isEmpty()) {
            throw new IllegalArgumentException(campo + " é obrigatório.");
        }
        return normalizado;
    }

    private static int inteiroObrigatorio(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException("Um identificador obrigatório não foi informado.");
        }
        return Integer.parseInt(valor.trim());
    }

    private static void validarEmail(String email) {
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Informe um e-mail válido.");
        }
    }

    private static String encode(String valor) {
        return URLEncoder.encode(valor == null ? "" : valor, StandardCharsets.UTF_8);
    }
}
