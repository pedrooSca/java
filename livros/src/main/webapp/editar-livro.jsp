<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.livraria.config.ConnectionFactory" %>
<%@ page import="com.livraria.controller.LivroController" %>
<%@ page import="com.livraria.dao.GeneroDAO" %>
<%@ page import="com.livraria.model.Livro" %>
<%@ page import="com.livraria.model.Genero" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Optional" %>
<%
    String idStr = request.getParameter("id");
    if (idStr == null || idStr.trim().isEmpty()) {
                response.sendRedirect("livros?erro=ID+do+livro+n%C3%A3o+informado.");
        return;
    }

    int id;
    try {
        id = Integer.parseInt(idStr.trim());
    } catch (NumberFormatException e) {
        response.sendRedirect("livros?erro=ID+inv%C3%A1lido.");
        return;
    }

    boolean dbOnline = false;
    Livro livro = null;
    List<Genero> generos = null;

    try {
        dbOnline = ConnectionFactory.testarConexao();
        if (dbOnline) {
            LivroController controller = new LivroController();
            GeneroDAO generoDAO = new GeneroDAO();
            generos = generoDAO.listarTodos();

            Optional<Livro> opt = controller.buscarPorId(id);
            if (opt.isPresent()) {
                livro = opt.get();
            } else {
                response.sendRedirect("livros?erro=Livro+n%C3%A3o+encontrado.");
                return;
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
    <title>Editar Livro #<%= id %> | Livraria</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
    <jsp:include page="navbar.jsp">
        <jsp:param name="activePage" value="livros"/>
    </jsp:include>

    <main class="container" style="padding: 10px 0 60px;">
        <div class="page-header">
            <a href="livros" style="font-size: 0.88rem; display: inline-flex; align-items: center; gap: 4px; margin-bottom: 12px;">
                ← Voltar para o Catálogo
            </a>
            <h1>Editar Livro #<%= id %></h1>
            <p>Atualize as informações do livro. A alteração disparará automaticamente o trigger de auditoria no MySQL.</p>
        </div>

        <% if (!dbOnline) { %>
            <div class="alert alert-error">
                O banco de dados não está respondendo. Não é possível editar livros no momento.
            </div>
        <% } else if (livro != null) { %>

        <div style="max-width: 680px; margin: 0 auto;">
            <!-- Aviso de Trigger -->
            <div class="alert alert-info">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="16" x2="12" y2="12"></line><line x1="12" y1="8" x2="12.01" y2="8"></line></svg>
                <div>
                    <strong>Trigger de Auditoria Ativo:</strong> Ao confirmar esta atualização, a trigger <code>trg_depois_atualizar_livro</code> registrará o título anterior e o novo na tabela <code>log_alteracoes_livros</code>.
                </div>
            </div>

            <div class="card">
                <form action="livros" method="post">
                    <input type="hidden" name="acao" value="atualizar_livro">
                    <input type="hidden" name="id" value="<%= livro.getId() %>">

                    <div class="form-group">
                        <label for="titulo" class="form-label">Título da Obra *</label>
                        <input type="text" id="titulo" name="titulo" value="<%= livro.getTitulo() %>" class="form-control" required autofocus>
                    </div>

                    <div class="form-group">
                        <label for="autor" class="form-label">Nome do Autor *</label>
                        <input type="text" id="autor" name="autor" value="<%= livro.getAutor() %>" class="form-control" required>
                    </div>

                    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
                        <div class="form-group">
                            <label for="generoId" class="form-label">Gênero Literário *</label>
                            <select id="generoId" name="generoId" class="form-control" required>
                                <% if (generos != null) {
                                    for (Genero g : generos) { 
                                        boolean sel = g.getId().equals(livro.getGeneroId());
                                %>
                                    <option value="<%= g.getId() %>" <%= sel ? "selected" : "" %>><%= g.getNome() %></option>
                                <%  } 
                                   } %>
                            </select>
                        </div>

                        <div class="form-group">
                            <label for="anoPublicacao" class="form-label">Ano de Publicação</label>
                            <input type="number" id="anoPublicacao" name="anoPublicacao" value="<%= livro.getAnoPublicacao() != null ? livro.getAnoPublicacao() : "" %>" class="form-control" min="1000" max="2100">
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="isbn" class="form-label">Código ISBN</label>
                        <input type="text" id="isbn" name="isbn" value="<%= livro.getIsbn() != null ? livro.getIsbn() : "" %>" class="form-control">
                    </div>

                    <div style="display: flex; justify-content: flex-end; gap: 12px; margin-top: 30px; border-top: 1px solid var(--line); padding-top: 20px;">
                        <a href="livros" class="btn btn-secondary">Cancelar</a>
                        <button type="submit" class="btn btn-primary">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"></path><polyline points="17 21 17 13 7 13 7 21"></polyline><polyline points="7 3 7 8 15 8"></polyline></svg>
                            Salvar Alterações
                        </button>
                    </div>
                </form>
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
