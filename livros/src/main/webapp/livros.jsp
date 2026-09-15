<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.livraria.config.ConnectionFactory" %>
<%@ page import="com.livraria.controller.LivroController" %>
<%@ page import="com.livraria.dao.GeneroDAO" %>
<%@ page import="com.livraria.dao.AvaliacaoDAO" %>
<%@ page import="com.livraria.model.LivroDetalhadoDTO" %>
<%@ page import="com.livraria.model.Genero" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%
    String q = request.getParameter("q");
    if (q == null) q = "";
    q = q.trim();

    String generoStr = request.getParameter("generoId");
    Integer generoFiltroId = null;
    if (generoStr != null && !generoStr.trim().isEmpty()) {
        try {
            generoFiltroId = Integer.parseInt(generoStr.trim());
        } catch (NumberFormatException ignored) {}
    }

    boolean dbOnline = false;
    List<LivroDetalhadoDTO> livros = null;
    List<Genero> generos = null;
    Map<Integer, Double> medias = null;

    try {
        dbOnline = ConnectionFactory.testarConexao();
        if (dbOnline) {
            LivroController controller = new LivroController();
            GeneroDAO generoDAO = new GeneroDAO();
            AvaliacaoDAO avaliacaoDAO = new AvaliacaoDAO();

            generos = generoDAO.listarTodos();
            medias = avaliacaoDAO.obterMediasPorLivros();

            if (!q.isEmpty() || (generoFiltroId != null && generoFiltroId > 0)) {
                livros = controller.pesquisarLivros(q, generoFiltroId);
            } else {
                livros = controller.listarLivrosDetalhados();
            }
        }
    } catch (Exception e) {
        dbOnline = false;
    }
%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Catálogo & Pesquisa de Livros | Livraria</title>
    <link rel="stylesheet" href="css/style.css">
    <script>
        function confirmarExclusao(id, titulo) {
            if (confirm("Tem certeza que deseja excluir o livro \"" + titulo + "\"?\n\nEssa ação acionará o trigger 'trg_depois_deletar_livro' para auditoria.")) {
                window.location.href = "acoes.jsp?acao=excluir_livro&id=" + id;
            }
        }
    </script>
</head>
<body>
    <jsp:include page="navbar.jsp">
        <jsp:param name="activePage" value="livros"/>
    </jsp:include>

    <main class="container" style="padding: 10px 0 60px;">
        <div class="page-header" style="display: flex; flex-wrap: wrap; justify-content: space-between; align-items: flex-end; gap: 16px;">
            <div>
                <h1>Catálogo de Livros</h1>
                <p>Pesquise títulos e autores, filtre por gênero e gerencie os livros com operações completas de CRUD e auditoria.</p>
            </div>
            <a href="novo-livro.jsp" class="btn btn-primary">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line></svg>
                Cadastrar Novo Livro
            </a>
        </div>

        <% if (!dbOnline) { %>
            <div class="alert alert-error">
                O banco de dados está temporariamente inacessível. Certifique-se de que os containers do MySQL e Tomcat estão ativos.
            </div>
        <% } else { %>

            <!-- Barra de Pesquisa e Filtros -->
            <div class="search-bar-container">
                <form action="livros.jsp" method="get" class="search-form">
                    <div class="search-input-wrap">
                        <input type="text" name="q" value="<%= q %>" class="form-control" placeholder="Buscar por título, autor ou ISBN..." autofocus>
                    </div>
                    <div class="search-select-wrap">
                        <select name="generoId" class="form-control">
                            <option value="">Todos os Gêneros</option>
                            <% if (generos != null) { 
                                for (Genero g : generos) { 
                                    boolean sel = generoFiltroId != null && generoFiltroId.equals(g.getId());
                            %>
                                <option value="<%= g.getId() %>" <%= sel ? "selected" : "" %>><%= g.getNome() %></option>
                            <%  } 
                               } %>
                        </select>
                    </div>
                    <button type="submit" class="btn btn-primary">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="8"></circle><line x1="21" y1="21" x2="16.65" y2="16.65"></line></svg>
                        Pesquisar
                    </button>
                    <% if (!q.isEmpty() || (generoFiltroId != null && generoFiltroId > 0)) { %>
                        <a href="livros.jsp" class="btn btn-secondary">Limpar Filtros</a>
                    <% } %>
                </form>
            </div>

            <!-- Informação da Busca -->
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
                <h3 style="font-size: 1.25rem;">
                    <% if (!q.isEmpty() || (generoFiltroId != null && generoFiltroId > 0)) { %>
                        Resultados da Pesquisa
                    <% } else { %>
                        Todos os Livros
                    <% } %>
                </h3>
                <span style="color: var(--muted); font-size: 0.92rem; font-weight: 500;">
                    <%= livros != null ? livros.size() : 0 %> livro(s) encontrado(s)
                </span>
            </div>

            <!-- Grid de Livros -->
            <% if (livros != null && !livros.isEmpty()) { %>
                <div class="book-grid">
                    <% for (LivroDetalhadoDTO livro : livros) { 
                        Double mediaNota = medias != null ? medias.get(livro.getLivroId()) : null;
                    %>
                    <article class="book-card">
                        <div>
                            <div class="book-header">
                                <span class="book-id">#<%= livro.getLivroId() %></span>
                                <span class="badge-genre"><%= livro.getGeneroNome() %></span>
                            </div>
                            <h2 class="book-title"><%= livro.getTitulo() %></h2>
                            <p class="book-author">Por <%= livro.getAutor() %></p>
                            
                            <div class="book-badges">
                                <% if (livro.getAnoPublicacao() != null) { %>
                                    <span class="badge-year">Ano: <%= livro.getAnoPublicacao() %></span>
                                <% } %>
                                <% if (livro.getIsbn() != null && !livro.getIsbn().isEmpty()) { %>
                                    <span class="badge-year" title="ISBN">ISBN: <%= livro.getIsbn() %></span>
                                <% } %>
                            </div>

                            <div class="book-rating">
                                <% if (mediaNota != null && mediaNota > 0) { 
                                    int rounded = (int) Math.round(mediaNota);
                                    StringBuilder stars = new StringBuilder();
                                    for (int s = 1; s <= 5; s++) {
                                        stars.append(s <= rounded ? "★" : "☆");
                                    }
                                %>
                                    <span class="stars"><%= stars.toString() %></span>
                                    <strong><%= String.format(java.util.Locale.US, "%.1f", mediaNota) %></strong>
                                <% } else { %>
                                    <span style="color: var(--muted); font-size: 0.85rem;">☆ Sem avaliações</span>
                                <% } %>
                            </div>
                        </div>

                        <div class="book-footer">
                            <div class="book-actions">
                                <a href="editar-livro.jsp?id=<%= livro.getLivroId() %>" class="btn btn-secondary btn-sm" title="Editar este livro">
                                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 20h9"></path><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"></path></svg>
                                    Editar
                                </a>
                                <a href="avaliar.jsp?livroId=<%= livro.getLivroId() %>" class="btn btn-secondary btn-sm" title="Avaliar este livro">
                                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon></svg>
                                    Avaliar
                                </a>
                            </div>
                            <button type="button" class="btn btn-danger btn-sm" onclick="confirmarExclusao(<%= livro.getLivroId() %>, '<%= livro.getTitulo().replace("'", "\\'") %>')" title="Excluir este livro">
                                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="3 6 5 6 21 6"></polyline><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path></svg>
                                Excluir
                            </button>
                        </div>
                    </article>
                    <% } %>
                </div>
            <% } else { %>
                <div class="card" style="text-align: center; padding: 48px 20px;">
                    <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="var(--muted)" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" style="margin-bottom: 12px;"><circle cx="11" cy="11" r="8"></circle><line x1="21" y1="21" x2="16.65" y2="16.65"></line></svg>
                    <h3>Nenhum livro encontrado</h3>
                    <p style="margin-top: 8px;">Tente pesquisar por outros termos ou remover os filtros aplicados.</p>
                    <div style="margin-top: 20px;">
                        <a href="livros.jsp" class="btn btn-secondary">Ver Catálogo Completo</a>
                        <a href="novo-livro.jsp" class="btn btn-primary" style="margin-left: 8px;">Cadastrar Novo Livro</a>
                    </div>
                </div>
            <% } %>

        <% } %>
    </main>

    <footer>
        <div class="container">
            Livraria · Sistema de Recomendações de Livros por Gênero · UNIUBE ADS MVC
        </div>
    </footer>
</body>
</html>
