<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.livraria.config.ConnectionFactory" %>
<%@ page import="com.livraria.dao.LivroDAO" %>
<%@ page import="com.livraria.dao.RecomendacaoDAO" %>
<%@ page import="com.livraria.model.LivroDetalhadoDTO" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Livraria | Catálogo de livros</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500;600;700&family=Playfair+Display:wght@600;700&display=swap" rel="stylesheet">
    <style>
        :root {
            --background: #f6f1e8;
            --surface: #fffdf8;
            --ink: #29251f;
            --muted: #756e63;
            --line: #e5ddd0;
            --accent: #a84f32;
            --tag: #e9f0e8;
            --tag-ink: #49624d;
        }
        * { box-sizing: border-box; margin: 0; padding: 0; }
        body { background: var(--background); color: var(--ink); font-family: 'DM Sans', sans-serif; line-height: 1.5; }
        .container { width: min(1120px, calc(100% - 32px)); margin: 0 auto; }
        header { padding: 42px 0 32px; border-bottom: 1px solid var(--line); }
        .brand { color: var(--accent); font-size: .82rem; font-weight: 700; letter-spacing: .12em; text-transform: uppercase; }
        h1, h2 { font-family: 'Playfair Display', serif; }
        h1 { font-size: clamp(2.2rem, 5vw, 4rem); line-height: 1.05; margin: 12px 0 10px; max-width: 720px; }
        .intro { color: var(--muted); max-width: 570px; font-size: 1.05rem; }
        main { padding: 34px 0 56px; }
        .section { margin-bottom: 42px; }
        .section-heading { align-items: end; display: flex; justify-content: space-between; gap: 16px; margin-bottom: 18px; }
        h2 { font-size: 1.75rem; }
        .count { color: var(--muted); font-size: .92rem; white-space: nowrap; }
        .book-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(245px, 1fr)); gap: 16px; }
        .book { background: var(--surface); border: 1px solid var(--line); border-radius: 8px; min-height: 190px; padding: 21px; transition: border-color .2s, transform .2s; }
        .book:hover { border-color: var(--accent); transform: translateY(-2px); }
        .book-number { color: var(--accent); font-size: .82rem; font-weight: 700; }
        .book h3 { font-family: 'Playfair Display', serif; font-size: 1.3rem; line-height: 1.2; margin: 12px 0 5px; }
        .author { color: var(--muted); font-size: .95rem; }
        .book-meta { align-items: center; border-top: 1px solid var(--line); display: flex; justify-content: space-between; margin-top: 24px; padding-top: 12px; }
        .genre { background: var(--tag); border-radius: 4px; color: var(--tag-ink); font-size: .78rem; font-weight: 600; padding: 4px 8px; }
        .year { color: var(--muted); font-size: .82rem; }
        .empty { background: var(--surface); border: 1px dashed var(--line); color: var(--muted); padding: 28px; text-align: center; }
        .notice { background: #fff8eb; border: 1px solid #eed9b2; border-radius: 8px; color: #795b28; padding: 18px 20px; }
        footer { border-top: 1px solid var(--line); color: var(--muted); font-size: .85rem; padding: 22px 0 30px; }
        @media (max-width: 600px) { header { padding-top: 28px; } .section-heading { align-items: start; flex-direction: column; gap: 4px; } }
    </style>
</head>
<body>
    <div class="container">
        <header>
            <div class="brand">Livraria</div>
            <h1>Encontre sua próxima leitura.</h1>
            <p class="intro">Explore o catálogo de livros e descubra recomendações selecionadas para você.</p>
        </header>
        <%
            boolean dbOnline = false;
            List<LivroDetalhadoDTO> livros = null;
            List<LivroDetalhadoDTO> recomendacoes = null;
            try {
                dbOnline = ConnectionFactory.testarConexao();
                if (dbOnline) {
                    livros = new LivroDAO().listarDetalhados();
                    recomendacoes = new RecomendacaoDAO().obterRecomendacoes(1);
                }
            } catch (Exception e) {
                dbOnline = false;
            }
        %>
        <main>
            <% if (!dbOnline) { %>
            <div class="notice">O catálogo está temporariamente indisponível. Tente novamente em alguns instantes.</div>
            <% } else { %>
                <section class="section">
                    <div class="section-heading"><h2>Catálogo</h2><span class="count"><%= livros != null ? livros.size() : 0 %> livros</span></div>
                    <% if (livros != null && !livros.isEmpty()) { %>
                    <div class="book-grid">
                        <% for (LivroDetalhadoDTO livro : livros) { %>
                        <article class="book">
                            <div class="book-number">#<%= livro.getLivroId() %></div>
                            <h3><%= livro.getTitulo() %></h3>
                            <p class="author"><%= livro.getAutor() %></p>
                            <div class="book-meta"><span class="genre"><%= livro.getGeneroNome() %></span><span class="year"><%= livro.getAnoPublicacao() != null ? livro.getAnoPublicacao() : "Ano não informado" %></span></div>
                        </article>
                        <% } %>
                    </div>
                    <% } else { %><div class="empty">Nenhum livro encontrado no catálogo.</div><% } %>
                </section>
                <section class="section">
                    <div class="section-heading"><h2>Recomendados para você</h2></div>
                    <% if (recomendacoes != null && !recomendacoes.isEmpty()) { %>
                    <div class="book-grid">
                        <% for (LivroDetalhadoDTO livro : recomendacoes) { %>
                        <article class="book"><h3><%= livro.getTitulo() %></h3><p class="author"><%= livro.getAutor() %></p><div class="book-meta"><span class="genre"><%= livro.getGeneroNome() %></span></div></article>
                        <% } %>
                    </div>
                    <% } else { %><div class="empty">Ainda não há recomendações disponíveis.</div><% } %>
                </section>
            <% } %>
        </main>
        <footer>Livraria · Um catálogo simples para boas histórias.</footer>
    </div>
</body>
</html>
