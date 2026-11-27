<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Recomendações Personalizadas | Livraria</title>
    <link rel="stylesheet" href="css/style.css?v=2">
<link rel="stylesheet" href="css/api.css"></head>
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

        <div id="api-message" role="status" aria-live="polite"></div><div id="api-content" data-page="recomendacoes"><div class="api-columns"><aside><div class="card"><h3>Usuário Ativo</h3><label for="active-user" class="form-label">Leitor</label><select id="active-user" class="form-control"></select><p id="user-email"></p></div><div class="card" style="margin-top:20px"><h3>Gêneros Preferidos</h3><div id="preferences"></div><form id="preference-form"><label for="preference-genre" class="form-label">Adicionar gênero</label><select id="preference-genre" name="generoId" class="form-control" required></select><button class="btn btn-primary btn-sm">OK</button></form></div></aside><section><h2 id="recommendation-title">Livros para você</h2><div id="books" class="book-grid"></div></section></div></div><noscript><div class="alert alert-warning">Ative JavaScript para utilizar o sistema.</div></noscript></main>

    <footer>
        <div class="container">
            Livraria · Sistema de Recomendações de Livros por Gênero · UNIUBE ADS MVC
        </div>
    </footer>
<script src="js/app.js?v=3" defer></script></body>
</html>
