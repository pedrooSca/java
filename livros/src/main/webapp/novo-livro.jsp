<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.livraria.config.ConnectionFactory" %>
<%@ page import="com.livraria.dao.GeneroDAO" %>
<%@ page import="com.livraria.model.Genero" %>
<%@ page import="java.util.List" %>
<%
    boolean dbOnline = false;
    List<Genero> generos = null;
    try {
        dbOnline = ConnectionFactory.testarConexao();
        if (dbOnline) {
            generos = new GeneroDAO().listarTodos();
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
    <title>Cadastrar Novo Livro | Livraria</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
    <jsp:include page="navbar.jsp">
        <jsp:param name="activePage" value="novo"/>
    </jsp:include>

    <main class="container" style="padding: 10px 0 60px;">
        <div class="page-header">
            <a href="livros.jsp" style="font-size: 0.88rem; display: inline-flex; align-items: center; gap: 4px; margin-bottom: 12px;">
                ← Voltar para o Catálogo
            </a>
            <h1>Cadastrar Novo Livro</h1>
            <p>Adicione um novo livro ao acervo da livraria preenchendo as informações abaixo.</p>
        </div>

        <% if (!dbOnline) { %>
            <div class="alert alert-error">
                O banco de dados não está respondendo. Não é possível cadastrar livros no momento.
            </div>
        <% } else { %>

        <div style="max-width: 680px; margin: 0 auto;">
            <div class="card">
                <form action="acoes.jsp" method="post">
                    <input type="hidden" name="acao" value="cadastrar_livro">

                    <div class="form-group">
                        <label for="titulo" class="form-label">Título da Obra *</label>
                        <input type="text" id="titulo" name="titulo" class="form-control" placeholder="Ex: Duna, O Senhor dos Anéis..." required autofocus>
                        <div class="form-hint">Informe o título completo da publicação.</div>
                    </div>

                    <div class="form-group">
                        <label for="autor" class="form-label">Nome do Autor *</label>
                        <input type="text" id="autor" name="autor" class="form-control" placeholder="Ex: Frank Herbert, J.R.R. Tolkien..." required>
                    </div>

                    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
                        <div class="form-group">
                            <label for="generoId" class="form-label">Gênero Literário *</label>
                            <select id="generoId" name="generoId" class="form-control" required>
                                <option value="" disabled selected>Selecione um gênero...</option>
                                <% if (generos != null) {
                                    for (Genero g : generos) { %>
                                    <option value="<%= g.getId() %>"><%= g.getNome() %></option>
                                <%  } 
                                   } %>
                            </select>
                            <div class="form-hint">Usado pelo algoritmo de recomendação.</div>
                        </div>

                        <div class="form-group">
                            <label for="anoPublicacao" class="form-label">Ano de Publicação</label>
                            <input type="number" id="anoPublicacao" name="anoPublicacao" class="form-control" placeholder="Ex: 1965" min="1000" max="2100">
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="isbn" class="form-label">Código ISBN (opcional)</label>
                        <input type="text" id="isbn" name="isbn" class="form-control" placeholder="Ex: 978-8576572008">
                    </div>

                    <div style="display: flex; justify-content: flex-end; gap: 12px; margin-top: 30px; border-top: 1px solid var(--line); padding-top: 20px;">
                        <a href="livros.jsp" class="btn btn-secondary">Cancelar</a>
                        <button type="submit" class="btn btn-primary">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"></polyline></svg>
                            Salvar Livro
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
