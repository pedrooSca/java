<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Auditoria — Log de Triggers | Livraria</title>
    <link rel="stylesheet" href="css/style.css?v=2">
<link rel="stylesheet" href="css/api.css"></head>
<body>
    <jsp:include page="navbar.jsp">
        <jsp:param name="activePage" value="auditoria"/>
    </jsp:include>

    <main class="container" style="padding: 10px 0 60px;">
        <div class="page-header">
            <h1>Log de Auditoria</h1>
            <p>
                Histórico de alterações gerado automaticamente pelas <strong>Triggers do MySQL</strong>:
                <code style="background: var(--accent-soft); color: var(--accent); padding: 2px 6px; border-radius: 4px; font-size: 0.86em;">trg_depois_atualizar_livro</code> e
                <code style="background: var(--accent-soft); color: var(--accent); padding: 2px 6px; border-radius: 4px; font-size: 0.86em;">trg_depois_deletar_livro</code>.
            </p>
        </div>

        <div id="api-message" role="status" aria-live="polite"></div><div id="api-content" data-page="auditoria"><div class="alert alert-info">As alterações e exclusões de livros são registradas automaticamente pelas Triggers do MySQL.</div><div id="audit-stats" class="stats-grid"></div><div class="table-responsive"><table class="data-table"><thead><tr><th>#</th><th>Ação</th><th>ID do Livro</th><th>Título Anterior</th><th>Título Novo</th><th>Data / Hora</th></tr></thead><tbody id="audit"></tbody></table></div></div><noscript><div class="alert alert-warning">Ative JavaScript para utilizar o sistema.</div></noscript></main>

    <footer>
        <div class="container">
            Livraria · Sistema de Recomendações de Livros por Gênero · UNIUBE ADS MVC
        </div>
    </footer>
<script src="js/app.js?v=3" defer></script></body>
</html>
