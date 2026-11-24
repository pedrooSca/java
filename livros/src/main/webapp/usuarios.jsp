<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Usuários & Preferências | Livraria</title>
    <link rel="stylesheet" href="css/style.css?v=2">
<link rel="stylesheet" href="css/api.css"></head>
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

        <div id="api-message" role="status" aria-live="polite"></div><div id="api-content" data-page="usuarios"><div class="api-columns"><section><h2>Usuários Cadastrados</h2><div id="users"></div></section><aside><div class="card"><h2>Gêneros Literários</h2><div id="genres"></div></div></aside></div></div><noscript><div class="alert alert-warning">Ative JavaScript para utilizar o sistema.</div></noscript></main>

    <footer>
        <div class="container">
            Livraria · Sistema de Recomendações de Livros por Gênero · UNIUBE ADS MVC
        </div>
    </footer>
<script src="js/app.js?v=3" defer></script></body>
</html>
