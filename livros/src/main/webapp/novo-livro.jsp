<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cadastrar Novo Livro | Livraria</title>
    <link rel="stylesheet" href="css/style.css?v=2">
<link rel="stylesheet" href="css/api.css"></head>
<body>
    <jsp:include page="navbar.jsp">
        <jsp:param name="activePage" value="novo"/>
    </jsp:include>

    <main class="container" style="padding: 10px 0 60px;">
        <div class="page-header">
            <a href="livros" style="font-size: 0.88rem; display: inline-flex; align-items: center; gap: 4px; margin-bottom: 12px;">
                ← Voltar para o Catálogo
            </a>
            <h1>Cadastrar Novo Livro</h1>
            <p>Adicione um novo livro ao acervo da livraria preenchendo as informações abaixo.</p>
        </div>

        <div id="api-message" role="status" aria-live="polite"></div><div id="api-content" data-page="novo-livro"><div style="max-width: 680px; margin: 0 auto;">
            <div class="card">
                <form id="book-form">


                    <div class="form-group">
                        <label for="titulo" class="form-label">Título da Obra *</label>
                        <input type="text" id="titulo" name="titulo" maxlength="150" class="form-control" placeholder="Ex: Duna, O Senhor dos Anéis..." required autofocus>
                        <div class="form-hint">Informe o título completo da publicação.</div>
                    </div>

                    <div class="form-group">
                        <label for="autor" class="form-label">Nome do Autor *</label>
                        <input type="text" id="autor" name="autor" maxlength="100" class="form-control" placeholder="Ex: Frank Herbert, J.R.R. Tolkien..." required>
                    </div>

                    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 16px;">
                        <div class="form-group">
                            <label for="generoId" class="form-label">Gênero Literário *</label>
                            <select id="generoId" name="generoId" class="form-control" required>
                                <option value="" disabled selected>Selecione um gênero...</option>

                            </select>
                            <div class="form-hint">Usado pelo algoritmo de recomendação.</div>
                        </div>

                        <div class="form-group">
                            <label for="anoPublicacao" class="form-label">Ano de Publicação</label>
                            <input type="number" id="anoPublicacao" name="anoPublicacao" class="form-control" placeholder="Ex: 1965" min="0">
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="isbn" class="form-label">Código ISBN (opcional)</label>
                        <input type="text" id="isbn" name="isbn" maxlength="20" class="form-control" placeholder="Ex: 978-8576572008">
                    </div>

                    <div style="display: flex; justify-content: flex-end; gap: 12px; margin-top: 30px; border-top: 1px solid var(--line); padding-top: 20px;">
                        <a href="livros" class="btn btn-secondary">Cancelar</a>
                        <button type="submit" class="btn btn-primary">
                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"></polyline></svg>
                            Salvar Livro
                        </button>
                    </div>
                </form>
            </div>
        </div>

        </div><noscript><div class="alert alert-warning">Ative JavaScript para utilizar o sistema.</div></noscript></main>

    <footer>
        <div class="container">
            Livraria · Sistema de Recomendações de Livros por Gênero · UNIUBE ADS MVC
        </div>
    </footer>
<script src="js/app.js?v=3" defer></script></body>
</html>
