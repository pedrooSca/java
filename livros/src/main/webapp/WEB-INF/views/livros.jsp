<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Catálogo & Pesquisa de Livros | Livraria</title>
    <link rel="stylesheet" href="css/style.css?v=2">

<link rel="stylesheet" href="css/api.css"></head>
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

        <div id="api-message" role="status" aria-live="polite"></div><div id="api-content" data-page="livros"><div class="search-bar-container"><form id="search-form" class="search-form"><div class="search-input-wrap"><input name="q" class="form-control" placeholder="Buscar por título, autor ou ISBN..."></div><div class="search-select-wrap"><select name="generoId" class="form-control"><option value="">Todos os Gêneros</option></select></div><button class="btn btn-primary">Pesquisar</button><a href="livros" class="btn btn-secondary">Limpar Filtros</a></form></div><p id="result-count"></p><div id="books" class="book-grid"></div></div><noscript><div class="alert alert-warning">Ative JavaScript para utilizar o sistema.</div></noscript></main>

    <footer>
        <div class="container">
            Livraria · Sistema de Recomendações de Livros por Gênero · UNIUBE ADS MVC
        </div>
    </footer>
<script src="js/app.js?v=6" defer></script></body>
</html>
