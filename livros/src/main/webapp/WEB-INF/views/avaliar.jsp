<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Avaliações de Livros | Livraria</title>
    <link rel="stylesheet" href="css/style.css?v=2">
    <style>
        .star-selector {
            display: flex;
            flex-direction: row-reverse;
            justify-content: flex-end;
            gap: 4px;
        }
        .star-selector input[type="radio"] { display: none; }
        .star-selector label {
            font-size: 2.2rem;
            color: #d1c4a8;
            cursor: pointer;
            transition: color 0.15s ease, transform 0.1s ease;
        }
        .star-selector label:hover,
        .star-selector label:hover ~ label,
        .star-selector input[type="radio"]:checked ~ label {
            color: var(--star);
        }
        .star-selector label:hover { transform: scale(1.15); }

        .review-card {
            background: var(--surface);
            border: 1px solid var(--line);
            border-radius: var(--radius-md);
            padding: 20px;
            display: grid;
            grid-template-columns: 1fr auto;
            gap: 12px;
            align-items: start;
            transition: box-shadow 0.2s ease;
        }
        .review-card:hover { box-shadow: var(--shadow-md); }
        .review-stars { color: var(--star); font-size: 1.1rem; letter-spacing: 2px; }
    </style>
<link rel="stylesheet" href="css/api.css"></head>
<body>
    <jsp:include page="navbar.jsp">
        <jsp:param name="activePage" value="avaliar"/>
    </jsp:include>

    <main class="container" style="padding: 10px 0 60px;">
        <div class="page-header">
            <h1>Avaliações & Resenhas</h1>
            <p>Registre sua opinião sobre os livros lidos e confira o que outros leitores acharam.</p>
        </div>

        <div id="api-message" role="status" aria-live="polite"></div><div id="api-content" data-page="avaliar"><div class="api-columns"><div class="card"><h2>Escreva sua Resenha</h2><form id="review-form"><p id="review-author">A avaliação será publicada com sua conta conectada.</p><div class="form-group"><label for="livroId" class="form-label">Livro Avaliado *</label><select id="livroId" name="livroId" class="form-control" required></select></div><div class="form-group"><label class="form-label">Nota *</label><div class="star-selector"><input type="radio" id="star5" name="nota" value="5" required><label for="star5" title="5 estrelas">★</label><input type="radio" id="star4" name="nota" value="4" required><label for="star4" title="4 estrelas">★</label><input type="radio" id="star3" name="nota" value="3" required><label for="star3" title="3 estrelas">★</label><input type="radio" id="star2" name="nota" value="2" required><label for="star2" title="2 estrelas">★</label><input type="radio" id="star1" name="nota" value="1" required><label for="star1" title="1 estrelas">★</label></div></div><div class="form-group"><label for="comentario" class="form-label">Resenha / Comentário</label><textarea id="comentario" name="comentario" rows="4" class="form-control" maxlength="10000"></textarea></div><button class="btn btn-primary">Publicar Avaliação</button></form></div><section><h2>Avaliações & Resenhas</h2><button id="all-reviews" class="btn btn-secondary btn-sm">Ver Todas</button><div id="reviews"></div></section></div></div><noscript><div class="alert alert-warning">Ative JavaScript para utilizar o sistema.</div></noscript></main>

    <footer>
        <div class="container">
            Livraria · Sistema de Recomendações de Livros por Gênero · UNIUBE ADS MVC
        </div>
    </footer>
<script src="js/app.js?v=5" defer></script></body>
</html>
