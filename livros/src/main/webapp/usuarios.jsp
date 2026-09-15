<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.livraria.config.ConnectionFactory" %>
<%@ page import="com.livraria.dao.GeneroDAO" %>
<%@ page import="com.livraria.dao.UsuarioDAO" %>
<%@ page import="com.livraria.model.Genero" %>
<%@ page import="com.livraria.model.Usuario" %>
<%@ page import="java.util.List" %>
<%
    boolean dbOnline = false;
    List<Usuario> usuarios = null;
    List<Genero> todosGeneros = null;

    try {
        dbOnline = ConnectionFactory.testarConexao();
        if (dbOnline) {
            usuarios = new UsuarioDAO().listarTodos();
            todosGeneros = new GeneroDAO().listarTodos();
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
    <title>Usuários & Preferências | Livraria</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
    <jsp:include page="navbar.jsp">
        <jsp:param name="activePage" value="usuarios"/>
    </jsp:include>

    <main class="container" style="padding: 10px 0 60px;">
        <div class="page-header" style="display: flex; flex-wrap: wrap; justify-content: space-between; align-items: flex-end; gap: 16px;">
            <div>
                <h1>Usuários & Preferências</h1>
                <p>Gerencie os usuários cadastrados e seus gêneros literários favoritos usados pelo algoritmo de recomendação.</p>
            </div>
        </div>

        <% if (!dbOnline) { %>
            <div class="alert alert-error">O banco de dados está temporariamente inacessível.</div>
        <% } else { %>

        <div style="display: grid; grid-template-columns: 1fr 360px; gap: 32px; align-items: start;">

            <!-- Lista de usuários -->
            <div>
                <h2 style="font-size: 1.25rem; margin-bottom: 18px;">Usuários Cadastrados (<%= usuarios != null ? usuarios.size() : 0 %>)</h2>
                <div style="display: flex; flex-direction: column; gap: 16px;">
                <% if (usuarios != null && !usuarios.isEmpty()) {
                    for (Usuario u : usuarios) {
                        List<Genero> prefs = u.getGenerosPreferidos();
                %>
                <div class="card" style="display: grid; grid-template-columns: 1fr auto; gap: 16px; align-items: start;">
                    <div>
                        <div style="display: flex; align-items: center; gap: 12px; margin-bottom: 8px;">
                            <div style="width: 42px; height: 42px; border-radius: 50%; background: var(--accent-soft); color: var(--accent); display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 1.1rem; font-family: 'Playfair Display', serif; flex-shrink: 0;">
                                <%= u.getNome().substring(0, 1).toUpperCase() %>
                            </div>
                            <div>
                                <div style="font-weight: 700; font-size: 1.05rem; color: var(--ink);"><%= u.getNome() %></div>
                                <div style="font-size: 0.85rem; color: var(--muted);"><%= u.getEmail() %></div>
                            </div>
                        </div>
                        <div style="font-size: 0.82rem; color: var(--muted); margin-bottom: 10px;">
                            Cadastrado em: <%= u.getDataCadastro() != null ? new java.text.SimpleDateFormat("dd/MM/yyyy").format(u.getDataCadastro()) : "—" %>
                        </div>
                        <div style="display: flex; flex-wrap: wrap; gap: 6px;">
                            <% if (prefs != null && !prefs.isEmpty()) {
                                for (Genero g : prefs) { %>
                                <span class="badge-genre"><%= g.getNome() %></span>
                            <%  }
                               } else { %>
                                <span style="font-size: 0.83rem; color: var(--muted);">Sem preferências definidas</span>
                            <% } %>
                        </div>
                    </div>
                    <a href="recomendacoes.jsp?usuarioId=<%= u.getId() %>" class="btn btn-secondary btn-sm" style="white-space: nowrap;">
                        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path></svg>
                        Ver Recomendações
                    </a>
                </div>
                <% } } else { %>
                    <div class="card" style="text-align: center; padding: 40px 20px;">
                        <p>Nenhum usuário cadastrado ainda.</p>
                    </div>
                <% } %>
                </div>
            </div>

            <!-- Cadastro de novo usuário -->
            <div>
                <div class="card">
                    <h2 style="font-size: 1.2rem; margin-bottom: 6px;">Cadastrar Usuário</h2>
                    <p style="font-size: 0.88rem; margin-bottom: 20px;">Adicione um novo leitor ao sistema.</p>

                    <form action="acoes.jsp" method="post">
                        <input type="hidden" name="acao" value="cadastrar_usuario">

                        <div class="form-group">
                            <label for="nome" class="form-label">Nome Completo *</label>
                            <input type="text" id="nome" name="nome" class="form-control" placeholder="Ex: João da Silva" required>
                        </div>

                        <div class="form-group">
                            <label for="email" class="form-label">E-mail *</label>
                            <input type="email" id="email" name="email" class="form-control" placeholder="Ex: joao@email.com" required>
                            <div class="form-hint">O e-mail deve ser único no sistema.</div>
                        </div>

                        <button type="submit" class="btn btn-primary btn-block">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="8.5" cy="7" r="4"></circle><line x1="20" y1="8" x2="20" y2="14"></line><line x1="23" y1="11" x2="17" y2="11"></line></svg>
                            Cadastrar Usuário
                        </button>
                    </form>
                </div>

                <!-- Gêneros Cadastrados -->
                <div class="card" style="margin-top: 20px;">
                    <h2 style="font-size: 1.1rem; margin-bottom: 14px;">Gêneros Literários</h2>
                    <div style="display: flex; flex-direction: column; gap: 8px;">
                        <% if (todosGeneros != null) {
                            for (Genero g : todosGeneros) { %>
                            <div style="display: flex; justify-content: space-between; align-items: center; padding: 8px 0; border-bottom: 1px solid var(--line);">
                                <div>
                                    <div style="font-weight: 600; font-size: 0.93rem; color: var(--ink);"><%= g.getNome() %></div>
                                    <div style="font-size: 0.8rem; color: var(--muted);"><%= g.getDescricao() != null ? g.getDescricao() : "" %></div>
                                </div>
                                <span class="badge-genre" style="margin-left: 8px;">#<%= g.getId() %></span>
                            </div>
                        <%  } } %>
                    </div>
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
