<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.livraria.config.ConnectionFactory" %>
<%@ page import="com.livraria.dao.AvaliacaoDAO" %>
<%@ page import="com.livraria.dao.LivroDAO" %>
<%@ page import="com.livraria.dao.UsuarioDAO" %>
<%@ page import="com.livraria.model.Avaliacao" %>
<%@ page import="com.livraria.model.LivroDetalhadoDTO" %>
<%@ page import="com.livraria.model.Usuario" %>
<%@ page import="java.util.List" %>
<%
    boolean dbOnline = false;
    List<LivroDetalhadoDTO> livros = null;
    List<Usuario> usuarios = null;
    List<Avaliacao> avaliacoes = null;

    String livroIdStr = request.getParameter("livroId");
    Integer livroSelecionadoId = null;
    if (livroIdStr != null && !livroIdStr.trim().isEmpty()) {
        try { livroSelecionadoId = Integer.parseInt(livroIdStr.trim()); } catch (NumberFormatException ignored) {}
    }

    try {
        dbOnline = ConnectionFactory.testarConexao();
        if (dbOnline) {
            livros = new LivroDAO().listarDetalhados();
            usuarios = new UsuarioDAO().listarTodos();
            AvaliacaoDAO avaliacaoDAO = new AvaliacaoDAO();
            if (livroSelecionadoId != null) {
                avaliacoes = avaliacaoDAO.listarPorLivro(livroSelecionadoId);
            } else {
                avaliacoes = avaliacaoDAO.listarTodas(50);
            }
        }
    } catch (Exception e) {
        dbOnline = false;
    }

    LivroDetalhadoDTO livroSelecionado = null;
    if (livroSelecionadoId != null && livros != null) {
        for (LivroDetalhadoDTO l : livros) {
            if (l.getLivroId() == livroSelecionadoId) { livroSelecionado = l; break; }
        }
    }
%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Avaliações de Livros | Livraria</title>
    <link rel="stylesheet" href="css/style.css">
    <style>
        .star-selector {
            display: flex;
            flex-direction: row-reverse;
            justify-content: flex-end;
            gap: 4px;
        }
        .star-selector input[type="radio"] { display: none; }
        .star-selector label {
            font-size: 2.2rem;
            color: #d1c4a8;
            cursor: pointer;
            transition: color 0.15s ease, transform 0.1s ease;
        }
        .star-selector label:hover,
        .star-selector label:hover ~ label,
        .star-selector input[type="radio"]:checked ~ label {
            color: var(--star);
        }
        .star-selector label:hover { transform: scale(1.15); }

        .review-card {
            background: var(--surface);
            border: 1px solid var(--line);
            border-radius: var(--radius-md);
            padding: 20px;
            display: grid;
            grid-template-columns: 1fr auto;
            gap: 12px;
            align-items: start;
            transition: box-shadow 0.2s ease;
        }
        .review-card:hover { box-shadow: var(--shadow-md); }
        .review-stars { color: var(--star); font-size: 1.1rem; letter-spacing: 2px; }
    </style>
</head>
<body>
    <jsp:include page="navbar.jsp">
        <jsp:param name="activePage" value="avaliar"/>
    </jsp:include>

    <main class="container" style="padding: 10px 0 60px;">
        <div class="page-header">
            <h1>Avaliações & Resenhas</h1>
            <p>Registre sua opinião sobre os livros lidos e confira o que outros leitores acharam.</p>
        </div>

        <% if (!dbOnline) { %>
            <div class="alert alert-error">
                O banco de dados está temporariamente inacessível.
            </div>
        <% } else { %>

        <div style="display: grid; grid-template-columns: 1fr 1.6fr; gap: 32px; align-items: start;">

            <!-- Painel Esquerdo: Formulário de Avaliação -->
            <div>
                <div class="card">
                    <h2 style="font-size: 1.4rem; margin-bottom: 6px;">Escreva sua Resenha</h2>
                    <p style="font-size: 0.9rem; margin-bottom: 22px;">Selecione o livro e dê sua nota de 1 a 5 estrelas.</p>

                    <form action="acoes" method="post">
                        <input type="hidden" name="acao" value="avaliar_livro">

                        <div class="form-group">
                            <label for="usuarioId" class="form-label">Seu Usuário *</label>
                            <select id="usuarioId" name="usuarioId" class="form-control" required>
                                <option value="" disabled selected>Selecione o usuário...</option>
                                <% if (usuarios != null) {
                                    for (Usuario u : usuarios) { %>
                                    <option value="<%= u.getId() %>"><%= u.getNome() %></option>
                                <%  } } %>
                            </select>
                        </div>

                        <div class="form-group">
                            <label for="livroId" class="form-label">Livro Avaliado *</label>
                            <select id="livroId" name="livroId" class="form-control" required onchange="this.form.submit()" style="display:none;">
                                <option value="">Selecione...</option>
                                <% if (livros != null) {
                                    for (LivroDetalhadoDTO l : livros) {
                                        boolean sel = livroSelecionadoId != null && livroSelecionadoId == l.getLivroId();
                                %>
                                    <option value="<%= l.getLivroId() %>" <%= sel ? "selected" : "" %>><%= l.getTitulo() %> — <%= l.getAutor() %></option>
                                <%  } } %>
                            </select>
                            <select id="livroIdVisual" class="form-control" onchange="document.getElementById('livroId').value=this.value; this.form.action='avaliar.jsp'; this.form.method='get'; this.form.submit();" required>
                                <option value="">Selecione um livro...</option>
                                <% if (livros != null) {
                                    for (LivroDetalhadoDTO l : livros) {
                                        boolean sel = livroSelecionadoId != null && livroSelecionadoId == l.getLivroId();
                                %>
                                    <option value="<%= l.getLivroId() %>" <%= sel ? "selected" : "" %>><%= l.getTitulo() %> — <%= l.getAutor() %></option>
                                <%  } } %>
                            </select>
                        </div>

                        <% if (livroSelecionado != null) { %>
                        <div style="background: var(--accent-soft); border-radius: var(--radius-sm); padding: 12px 16px; margin-bottom: 20px;">
                            <strong style="color: var(--accent); font-size: 0.85rem;">Avaliando:</strong><br>
                            <strong><%= livroSelecionado.getTitulo() %></strong> · <span style="color: var(--muted)"><%= livroSelecionado.getAutor() %></span>
                        </div>
                        <% } %>

                        <div class="form-group">
                            <label class="form-label">Nota *</label>
                            <div class="star-selector">
                                <input type="radio" id="star5" name="nota" value="5" required><label for="star5" title="5 estrelas">★</label>
                                <input type="radio" id="star4" name="nota" value="4"><label for="star4" title="4 estrelas">★</label>
                                <input type="radio" id="star3" name="nota" value="3"><label for="star3" title="3 estrelas">★</label>
                                <input type="radio" id="star2" name="nota" value="2"><label for="star2" title="2 estrelas">★</label>
                                <input type="radio" id="star1" name="nota" value="1"><label for="star1" title="1 estrela">★</label>
                            </div>
                        </div>

                        <div class="form-group">
                            <label for="comentario" class="form-label">Resenha / Comentário</label>
                            <textarea id="comentario" name="comentario" class="form-control" rows="4" placeholder="O que você achou deste livro? Compartilhe sua opinião..."></textarea>
                        </div>

                        <button type="submit" class="btn btn-primary btn-block">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon></svg>
                            Publicar Avaliação
                        </button>
                    </form>
                </div>
            </div>

            <!-- Painel Direito: Mural de Avaliações -->
            <div>
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 18px;">
                    <h2 style="font-size: 1.4rem;">
                        <% if (livroSelecionado != null) { %>
                            Resenhas: <%= livroSelecionado.getTitulo() %>
                        <% } else { %>
                            Avaliações Recentes
                        <% } %>
                    </h2>
                    <% if (livroSelecionadoId != null) { %>
                        <a href="avaliar.jsp" class="btn btn-secondary btn-sm">Ver Todas</a>
                    <% } %>
                </div>

                <div style="display: flex; flex-direction: column; gap: 14px;">
                    <% if (avaliacoes == null || avaliacoes.isEmpty()) { %>
                        <div class="card" style="text-align: center; padding: 36px 20px;">
                            <p>Nenhuma avaliação registrada ainda. Seja o primeiro a avaliar!</p>
                        </div>
                    <% } else {
                        for (Avaliacao av : avaliacoes) {
                            StringBuilder starsHtml = new StringBuilder();
                            for (int s = 1; s <= 5; s++) {
                                starsHtml.append(s <= av.getNota() ? "★" : "☆");
                            }
                    %>
                    <div class="review-card">
                        <div>
                            <div style="display: flex; align-items: center; gap: 10px; margin-bottom: 6px;">
                                <strong style="font-size: 0.95rem;"><%= av.getUsuarioNome() %></strong>
                                <span class="review-stars"><%= starsHtml.toString() %></span>
                            </div>
                            <div style="font-size: 0.82rem; color: var(--muted); margin-bottom: 8px;">
                                sobre <em><%= av.getLivroTitulo() %></em>
                            </div>
                            <% if (av.getComentario() != null && !av.getComentario().isEmpty()) { %>
                            <p style="font-size: 0.93rem; color: var(--ink); line-height: 1.5;">"<%= av.getComentario() %>"</p>
                            <% } %>
                        </div>
                        <div style="color: var(--muted); font-size: 0.8rem; white-space: nowrap;">
                            <%= av.getDataAvaliacao() != null ? new java.text.SimpleDateFormat("dd/MM/yyyy").format(av.getDataAvaliacao()) : "" %>
                        </div>
                    </div>
                    <% } } %>
                </div>
            </div>
        </div>

        <% } %>
    </main>

    <footer>
        <div class="container">
            Livraria · Sistema de Recomendações de Livros por Gênero · UNIUBE ADS MVC
        </div>
    </footer>
</body>
</html>
