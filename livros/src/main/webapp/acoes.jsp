<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.livraria.controller.LivroController" %>
<%@ page import="com.livraria.dao.AvaliacaoDAO" %>
<%@ page import="com.livraria.dao.UsuarioDAO" %>
<%@ page import="com.livraria.model.Avaliacao" %>
<%@ page import="com.livraria.model.Usuario" %>
<%@ page import="java.net.URLEncoder" %>
<%
    request.setCharacterEncoding("UTF-8");
    String acao = request.getParameter("acao");

    if (acao == null || acao.isEmpty()) {
        response.sendRedirect("index.jsp");
        return;
    }

    LivroController livroController = new LivroController();
    AvaliacaoDAO avaliacaoDAO = new AvaliacaoDAO();
    UsuarioDAO usuarioDAO = new UsuarioDAO();

    try {
        if ("cadastrar_livro".equals(acao)) {
            String titulo = request.getParameter("titulo");
            String autor = request.getParameter("autor");
            String isbn = request.getParameter("isbn");
            String anoStr = request.getParameter("anoPublicacao");
            String generoStr = request.getParameter("generoId");

            Integer ano = (anoStr != null && !anoStr.trim().isEmpty()) ? Integer.parseInt(anoStr.trim()) : null;
            int generoId = Integer.parseInt(generoStr.trim());

            livroController.cadastrarLivro(titulo, autor, isbn, ano, generoId);
            response.sendRedirect("livros.jsp?msg=livro_criado");
            return;

        } else if ("atualizar_livro".equals(acao)) {
            int id = Integer.parseInt(request.getParameter("id").trim());
            String titulo = request.getParameter("titulo");
            String autor = request.getParameter("autor");
            String isbn = request.getParameter("isbn");
            String anoStr = request.getParameter("anoPublicacao");
            String generoStr = request.getParameter("generoId");

            Integer ano = (anoStr != null && !anoStr.trim().isEmpty()) ? Integer.parseInt(anoStr.trim()) : null;
            int generoId = Integer.parseInt(generoStr.trim());

            livroController.atualizarLivro(id, titulo, autor, isbn, ano, generoId);
            response.sendRedirect("livros.jsp?msg=livro_atualizado");
            return;

        } else if ("excluir_livro".equals(acao)) {
            int id = Integer.parseInt(request.getParameter("id").trim());
            livroController.excluirLivro(id);
            response.sendRedirect("livros.jsp?msg=livro_excluido");
            return;

        } else if ("avaliar_livro".equals(acao)) {
            int usuarioId = Integer.parseInt(request.getParameter("usuarioId").trim());
            int livroId = Integer.parseInt(request.getParameter("livroId").trim());
            int nota = Integer.parseInt(request.getParameter("nota").trim());
            String comentario = request.getParameter("comentario");

            Avaliacao av = new Avaliacao(usuarioId, livroId, nota, comentario != null ? comentario.trim() : "");
            avaliacaoDAO.inserir(av);
            response.sendRedirect("avaliar.jsp?livroId=" + livroId + "&msg=avaliacao_criada");
            return;

        } else if ("cadastrar_usuario".equals(acao)) {
            String nome = request.getParameter("nome");
            String email = request.getParameter("email");

            if (nome == null || nome.trim().isEmpty() || email == null || email.trim().isEmpty()) {
                throw new IllegalArgumentException("Nome e e-mail são obrigatórios.");
            }

            Usuario u = new Usuario(nome.trim(), email.trim());
            usuarioDAO.inserir(u);
            response.sendRedirect("usuarios.jsp?msg=usuario_criado");
            return;

        } else if ("adicionar_preferencia".equals(acao)) {
            int usuarioId = Integer.parseInt(request.getParameter("usuarioId").trim());
            int generoId = Integer.parseInt(request.getParameter("generoId").trim());
            usuarioDAO.adicionarPreferencia(usuarioId, generoId);
            response.sendRedirect("recomendacoes.jsp?usuarioId=" + usuarioId + "&msg=preferencia_atualizada");
            return;

        } else if ("remover_preferencia".equals(acao)) {
            int usuarioId = Integer.parseInt(request.getParameter("usuarioId").trim());
            int generoId = Integer.parseInt(request.getParameter("generoId").trim());
            usuarioDAO.removerPreferencia(usuarioId, generoId);
            response.sendRedirect("recomendacoes.jsp?usuarioId=" + usuarioId + "&msg=preferencia_atualizada");
            return;
        }

        response.sendRedirect("index.jsp");

    } catch (Exception e) {
        String erroMsg = URLEncoder.encode(e.getMessage() != null ? e.getMessage() : "Erro ao processar requisição.", "UTF-8");
        String origem = request.getHeader("Referer");
        if (origem != null && !origem.isEmpty()) {
            String separador = origem.contains("?") ? "&" : "?";
            response.sendRedirect(origem + separador + "erro=" + erroMsg);
        } else {
            response.sendRedirect("index.jsp?erro=" + erroMsg);
        }
    }
%>
