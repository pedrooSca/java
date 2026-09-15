<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.livraria.config.ConnectionFactory" %>
<%@ page import="com.livraria.controller.RecomendacaoController" %>
<%@ page import="com.livraria.dao.GeneroDAO" %>
<%@ page import="com.livraria.dao.UsuarioDAO" %>
<%@ page import="com.livraria.model.Genero" %>
<%@ page import="com.livraria.model.LivroDetalhadoDTO" %>
<%@ page import="com.livraria.model.Usuario" %>
<%@ page import="java.util.List" %>
<%
    boolean dbOnline = false;
    List<Usuario> usuarios = null;
    List<LivroDetalhadoDTO> recomendacoes = null;
    List<Genero> generos = null;
    List<Genero> preferenciasDoUsuario = null;

    String usuarioIdStr = request.getParameter("usuarioId");
    Integer usuarioSelecionadoId = null;
    if (usuarioIdStr != null && !usuarioIdStr.trim().isEmpty()) {
        try { usuarioSelecionadoId = Integer.parseInt(usuarioIdStr.trim()); } catch (NumberFormatException ignored) {}
    }

    Usuario usuarioSelecionado = null;

    try {
        dbOnline = ConnectionFactory.testarConexao();
        if (dbOnline) {
            UsuarioDAO usuarioDAO = new UsuarioDAO();
            GeneroDAO generoDAO = new GeneroDAO();
            generos = generoDAO.listarTodos();
            usuarios = usuarioDAO.listarTodos();

            if (usuarioSelecionadoId == null && usuarios != null && !usuarios.isEmpty()) {
                usuarioSelecionadoId = usuarios.get(0).getId();
            }

            if (usuarioSelecionadoId != null) {
                for (Usuario u : usuarios) {
                    if (u.getId() == usuarioSelecionadoId) { usuarioSelecionado = u; break; }
                }
                recomendacoes = new RecomendacaoController().obterRecomendacoes(usuarioSelecionadoId);
                preferenciasDoUsuario = usuarioDAO.obterGenerosPreferidos(usuarioSelecionadoId);
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
    <title>Recomendações Personalizadas | Livraria</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
    <jsp:include page="navbar.jsp">
        <jsp:param name="activePage" value="recomendacoes"/>
    </jsp:include>

    <main class="container" style="padding: 10px 0 60px;">
        <div class="page-header">
            <h1>Recomendações Personalizadas</h1>
            <p>
                Baseadas nos gêneros preferidos de cada usuário, executadas via <strong>Stored Procedure</strong>
                <code style="background: var(--accent-soft); color: var(--accent); padding: 2px 6px; border-radius: 4px; font-size: 0.88em;">sp_obter_recomendacoes_usuario</code>.
            </p>
        </div>

        <% if (!dbOnline) { %>
            <div class="alert alert-error">O banco de dados está temporariamente inacessível.</div>
        <% } else { %>

        <div style="display: grid; grid-template-columns: 300px 1fr; gap: 32px; align-items: start;">

            <!-- Coluna Esquerda: Seleção de usuário e preferências -->
            <div>
                <!-- Seleção de Usuário -->
                <div class="card" style="margin-bottom: 20px;">
                    <h3 style="font-size: 1.1rem; margin-bottom: 16px;">Usuário Ativo</h3>
                    <form action="recomendacoes.jsp" method="get">
                        <div class="form-group" style="margin-bottom: 12px;">
                            <select name="usuarioId" class="form-control" onchange="this.form.submit()">
                                <% if (usuarios != null) {
                                    for (Usuario u : usuarios) {
                                        boolean sel = usuarioSelecionadoId != null && usuarioSelecionadoId == u.getId();
                                %>
                                    <option value="<%= u.getId() %>" <%= sel ? "selected" : "" %>><%= u.getNome() %></option>
                                <%  } } %>
                            </select>
                        </div>
                    </form>
                    <% if (usuarioSelecionado != null) { %>
                        <div style="font-size: 0.85rem; color: var(--muted);">
                            <div><strong style="color: var(--ink);">E-mail:</strong> <%= usuarioSelecionado.getEmail() %></div>
                        </div>
                    <% } %>
                </div>

                <!-- Preferências do Usuário -->
                <div class="card">
                    <h3 style="font-size: 1.1rem; margin-bottom: 4px;">Gêneros Preferidos</h3>
                    <p style="font-size: 0.85rem; margin-bottom: 16px;">Os livros recomendados são do(s) gênero(s) abaixo.</p>

                    <% if (preferenciasDoUsuario != null && !preferenciasDoUsuario.isEmpty()) { %>
                        <div style="display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 16px;">
                            <% for (Genero g : preferenciasDoUsuario) { %>
                            <div style="display: flex; align-items: center; gap: 6px; background: var(--tag); border-radius: 20px; padding: 4px 12px 4px 6px;">
                                <span style="font-size: 0.82rem; font-weight: 600; color: var(--tag-ink);"><%= g.getNome() %></span>
                                <form action="acoes.jsp" method="post" style="display: inline;">
                                    <input type="hidden" name="acao" value="remover_preferencia">
                                    <input type="hidden" name="usuarioId" value="<%= usuarioSelecionadoId %>">
                                    <input type="hidden" name="generoId" value="<%= g.getId() %>">
                                    <button type="submit" title="Remover preferência" style="background: none; border: none; cursor: pointer; color: var(--muted); font-size: 1rem; line-height: 1; padding: 0;">×</button>
                                </form>
                            </div>
                            <% } %>
                        </div>
                    <% } else { %>
                        <p style="font-size: 0.88rem; color: var(--muted); margin-bottom: 16px;">Nenhuma preferência registrada.</p>
                    <% } %>

                    <!-- Adicionar preferência -->
                    <% 
                        // Montar lista de gêneros que o usuário ainda não tem
                        java.util.Set<Integer> preferidosSet = new java.util.HashSet<>();
                        if (preferenciasDoUsuario != null) {
                            for (Genero g : preferenciasDoUsuario) { preferidosSet.add(g.getId()); }
                        }
                        boolean temGenerosDisponiveis = generos != null && generos.stream().anyMatch(g -> !preferidosSet.contains(g.getId()));
                    %>
                    <% if (temGenerosDisponiveis) { %>
                    <form action="acoes.jsp" method="post" style="display: flex; gap: 8px; margin-top: 4px;">
                        <input type="hidden" name="acao" value="adicionar_preferencia">
                        <input type="hidden" name="usuarioId" value="<%= usuarioSelecionadoId %>">
                        <select name="generoId" class="form-control" style="flex: 1; padding: 8px 10px;" required>
                            <option value="">+ Adicionar gênero</option>
                            <% if (generos != null) {
                                for (Genero g : generos) {
                                    if (!preferidosSet.contains(g.getId())) { %>
                                    <option value="<%= g.getId() %>"><%= g.getNome() %></option>
                            <%      }
                                }
                            } %>
                        </select>
                        <button type="submit" class="btn btn-primary btn-sm">OK</button>
                    </form>
                    <% } %>
                </div>
            </div>

            <!-- Coluna Direita: Livros Recomendados -->
            <div>
                <div style="display: flex; align-items: center; gap: 14px; margin-bottom: 20px;">
                    <h2 style="font-size: 1.4rem;">
                        Livros para <%= usuarioSelecionado != null ? usuarioSelecionado.getNome() : "você" %>
                    </h2>
                    <% if (recomendacoes != null) { %>
                    <span style="background: var(--accent); color: white; border-radius: 20px; padding: 2px 10px; font-size: 0.82rem; font-weight: 700;">
                        <%= recomendacoes.size() %> recomendado(s)
                    </span>
                    <% } %>
                </div>

                <% if (recomendacoes == null || recomendacoes.isEmpty()) { %>
                    <div class="card" style="text-align: center; padding: 48px 24px;">
                        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="var(--muted)" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" style="margin-bottom: 16px;"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path></svg>
                        <h3 style="margin-bottom: 8px;">Sem recomendações</h3>
                        <p>Este usuário não possui gêneros literários cadastrados como preferência. Adicione gêneros no painel ao lado.</p>
                    </div>
                <% } else { %>
                    <div class="book-grid" style="grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));">
                        <% for (LivroDetalhadoDTO livro : recomendacoes) { %>
                        <article class="book-card">
                            <div>
                                <div style="display: flex; align-items: center; gap: 8px; margin-bottom: 10px;">
                                    <span class="badge-genre"><%= livro.getGeneroNome() %></span>
                                    <span style="background: var(--accent-soft); color: var(--accent); font-size: 0.75rem; font-weight: 700; padding: 3px 8px; border-radius: 12px;">✦ Rec.</span>
                                </div>
                                <h3 class="book-title"><%= livro.getTitulo() %></h3>
                                <p class="book-author">Por <%= livro.getAutor() %></p>
                                <% if (livro.getAnoPublicacao() != null) { %>
                                <span class="badge-year" style="margin-top: 10px; display: inline-block;">Ano: <%= livro.getAnoPublicacao() %></span>
                                <% } %>
                            </div>
                            <div class="book-footer" style="margin-top: 16px;">
                                <a href="avaliar.jsp?livroId=<%= livro.getLivroId() %>" class="btn btn-secondary btn-sm">
                                    <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon></svg>
                                    Avaliar
                                </a>
                            </div>
                        </article>
                        <% } %>
                    </div>
                <% } %>
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
