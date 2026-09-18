<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.livraria.model.Avaliacao" %>
<%@ page import="com.livraria.model.LivroDetalhadoDTO" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%
    if (request.getAttribute("homeLoaded") == null) {
        response.sendRedirect(request.getContextPath() + "/home");
        return;
    }
    boolean dbOnline = Boolean.TRUE.equals(request.getAttribute("dbOnline"));
    List<LivroDetalhadoDTO> livros = (List<LivroDetalhadoDTO>) request.getAttribute("livros");
    List<LivroDetalhadoDTO> recomendacoes = (List<LivroDetalhadoDTO>) request.getAttribute("recomendacoes");
    List<Avaliacao> avaliacoesRecentes = (List<Avaliacao>) request.getAttribute("avaliacoesRecentes");
    Map<Integer, Double> medias = (Map<Integer, Double>) request.getAttribute("medias");
    Integer totalLivrosAttr = (Integer) request.getAttribute("totalLivros");
    Integer totalGenerosAttr = (Integer) request.getAttribute("totalGeneros");
    Integer totalUsuariosAttr = (Integer) request.getAttribute("totalUsuarios");
    Integer totalAvaliacoesAttr = (Integer) request.getAttribute("totalAvaliacoes");
    int totalLivros = totalLivrosAttr == null ? 0 : totalLivrosAttr;
    int totalGeneros = totalGenerosAttr == null ? 0 : totalGenerosAttr;
    int totalUsuarios = totalUsuariosAttr == null ? 0 : totalUsuariosAttr;
    int totalAvaliacoes = totalAvaliacoesAttr == null ? 0 : totalAvaliacoesAttr;
%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Sistema de Recomendação de Livros por Gênero — UNIUBE ADS MVC com MySQL, Views, Triggers e Stored Procedures">
    <title>Livraria | Sistema de Recomendação de Livros</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=DM+Sans:ital,opsz,wght@0,9..40,400;0,9..40,500;0,9..40,600;0,9..40,700;1,9..40,400&family=Playfair+Display:ital,wght@0,600;0,700;1,600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="css/style.css">
    <style>
        .hero {
            background: linear-gradient(135deg, #2d2218 0%, #4a3323 50%, #a84f32 100%);
            color: white;
            padding: 72px 0 60px;
            margin-bottom: 40px;
            position: relative;
            overflow: hidden;
        }
        .hero::before {
            content: '';
            position: absolute;
            inset: 0;
            background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.03'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E") repeat;
            pointer-events: none;
        }
        .hero h1 { color: white; font-size: clamp(2.4rem, 5vw, 4rem); margin-bottom: 14px; }
        .hero p { color: rgba(255,255,255,0.75); font-size: 1.1rem; max-width: 600px; }
        .hero-actions { display: flex; flex-wrap: wrap; gap: 12px; margin-top: 30px; }
        .btn-hero { padding: 12px 24px; font-size: 1rem; border-radius: var(--radius-sm); }
        .btn-hero-primary { background: white; color: var(--accent); font-weight: 700; }
        .btn-hero-primary:hover { background: #f5f0e8; color: var(--accent-hover); transform: translateY(-2px); box-shadow: 0 6px 20px rgba(0,0,0,0.2); }
        .btn-hero-secondary { background: rgba(255,255,255,0.12); color: white; border: 1px solid rgba(255,255,255,0.3); }
        .btn-hero-secondary:hover { background: rgba(255,255,255,0.2); color: white; transform: translateY(-2px); }
        .section-title { font-size: 1.5rem; margin-bottom: 20px; display: flex; align-items: center; gap: 12px; }
        .section-title span { color: var(--muted); font-size: 0.9rem; font-family: 'DM Sans', sans-serif; font-weight: 500; }
        .rec-card { background: var(--surface); border: 1px solid var(--line); border-radius: var(--radius-md); padding: 18px; transition: transform 0.2s ease, box-shadow 0.2s ease; }
        .rec-card:hover { transform: translateY(-2px); box-shadow: var(--shadow-md); }
        .quick-links { display: grid; grid-template-columns: repeat(auto-fill, minmax(200px, 1fr)); gap: 14px; margin-bottom: 40px; }
        .quick-link { background: var(--surface); border: 1px solid var(--line); border-radius: var(--radius-md); padding: 20px; text-decoration: none; transition: all 0.2s ease; }
        .quick-link:hover { border-color: var(--accent); background: var(--accent-soft); transform: translateY(-2px); box-shadow: var(--shadow-md); }
        .quick-link-icon { font-size: 1.6rem; margin-bottom: 10px; }
        .quick-link-title { font-weight: 700; font-size: 0.97rem; color: var(--ink); margin-bottom: 4px; }
        .quick-link-desc { font-size: 0.82rem; color: var(--muted); }
    </style>
</head>
<body>
    <jsp:include page="navbar.jsp">
        <jsp:param name="activePage" value="index"/>
    </jsp:include>

    <!-- Hero Banner -->
    <section class="hero">
        <div class="container">
            <div style="font-size: 0.82rem; font-weight: 700; letter-spacing: 0.12em; text-transform: uppercase; color: rgba(255,255,255,0.55); margin-bottom: 12px;">UNIUBE · ADS · Projeto MVC</div>
            <h1>Encontre sua próxima<br><em>leitura favorita.</em></h1>
            <p>Sistema de gerenciamento e recomendação de livros com MySQL, Views, Triggers e Stored Procedures.</p>
            <div class="hero-actions">
                <a href="livros" class="btn btn-hero btn-hero-primary">Explorar Catálogo</a>
                <a href="recomendacoes.jsp" class="btn btn-hero btn-hero-secondary">Ver Recomendações</a>
                <a href="novo-livro.jsp" class="btn btn-hero btn-hero-secondary">+ Cadastrar Livro</a>
            </div>
        </div>
    </section>

    <main class="container" style="padding-bottom: 60px;">

        <% if (!dbOnline) { %>
            <div class="alert alert-error">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
                <div><strong>Banco de dados inacessível.</strong> Verifique se os containers Docker estão ativos: <code>docker compose up -d</code></div>
            </div>
        <% } else { %>

        <!-- Métricas Rápidas -->
        <div class="stats-grid">
            <div class="stat-card">
                <div class="stat-value"><%= totalLivros %></div>
                <div class="stat-label">Livros no acervo</div>
            </div>
            <div class="stat-card">
                <div class="stat-value"><%= totalGeneros %></div>
                <div class="stat-label">Gêneros literários</div>
            </div>
            <div class="stat-card">
                <div class="stat-value"><%= totalUsuarios %></div>
                <div class="stat-label">Leitores cadastrados</div>
            </div>
            <div class="stat-card">
                <div class="stat-value"><%= totalAvaliacoes %></div>
                <div class="stat-label">Avaliações registradas</div>
            </div>
        </div>

        <!-- Acesso Rápido -->
        <h2 class="section-title">Acesso Rápido</h2>
        <div class="quick-links">
            <a href="livros" class="quick-link">
                <div class="quick-link-icon">📚</div>
                <div class="quick-link-title">Catálogo & Pesquisa</div>
                <div class="quick-link-desc">Busque títulos, autores e filtre por gênero</div>
            </a>
            <a href="novo-livro.jsp" class="quick-link">
                <div class="quick-link-icon">➕</div>
                <div class="quick-link-title">Cadastrar Livro</div>
                <div class="quick-link-desc">Adicione novos títulos ao acervo</div>
            </a>
            <a href="avaliar.jsp" class="quick-link">
                <div class="quick-link-icon">⭐</div>
                <div class="quick-link-title">Avaliações</div>
                <div class="quick-link-desc">Dê notas e escreva resenhas</div>
            </a>
            <a href="recomendacoes.jsp" class="quick-link">
                <div class="quick-link-icon">💡</div>
                <div class="quick-link-title">Recomendações</div>
                <div class="quick-link-desc">Stored Procedure por gênero preferido</div>
            </a>
            <a href="usuarios.jsp" class="quick-link">
                <div class="quick-link-icon">👥</div>
                <div class="quick-link-title">Usuários</div>
                <div class="quick-link-desc">Gerencie leitores e preferências</div>
            </a>
            <a href="auditoria.jsp" class="quick-link">
                <div class="quick-link-icon">📋</div>
                <div class="quick-link-title">Auditoria</div>
                <div class="quick-link-desc">Logs gerados pelos Triggers MySQL</div>
            </a>
        </div>

        <!-- Recomendações para Aluno ADS -->
        <% if (recomendacoes != null && !recomendacoes.isEmpty()) { %>
        <h2 class="section-title">
            Recomendações para Aluno ADS <span>via Stored Procedure</span>
        </h2>
        <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 14px; margin-bottom: 40px;">
            <% for (LivroDetalhadoDTO rec : recomendacoes) { %>
            <div class="rec-card">
                <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 8px;">
                    <span class="badge-genre"><%= rec.getGeneroNome() %></span>
                    <span style="background: var(--accent-soft); color: var(--accent); font-size: 0.72rem; font-weight: 700; padding: 2px 7px; border-radius: 10px;">✦ Rec.</span>
                </div>
                <h3 style="font-family: 'Playfair Display', serif; font-size: 1.1rem; line-height: 1.25; margin-bottom: 6px;"><%= rec.getTitulo() %></h3>
                <p style="font-size: 0.87rem;">por <%= rec.getAutor() %></p>
            </div>
            <% } %>
        </div>
        <% } %>

        <!-- Avaliações Recentes -->
        <% if (avaliacoesRecentes != null && !avaliacoesRecentes.isEmpty()) { %>
        <h2 class="section-title">
            Avaliações Recentes <span><%= avaliacoesRecentes.size() %> comentários</span>
        </h2>
        <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 14px; margin-bottom: 40px;">
            <% for (Avaliacao av : avaliacoesRecentes) {
                StringBuilder stars = new StringBuilder();
                for (int s = 1; s <= 5; s++) { stars.append(s <= av.getNota() ? "★" : "☆"); }
            %>
            <div class="rec-card">
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px;">
                    <strong style="font-size: 0.93rem;"><%= av.getUsuarioNome() %></strong>
                    <span style="color: var(--star); font-size: 1rem;"><%= stars.toString() %></span>
                </div>
                <div style="font-size: 0.8rem; color: var(--muted); margin-bottom: 8px;">sobre <em><%= av.getLivroTitulo() %></em></div>
                <% if (av.getComentario() != null && !av.getComentario().isEmpty()) { %>
                <p style="font-size: 0.88rem; font-style: italic; color: var(--ink); line-height: 1.5;">
                    "<%= av.getComentario().length() > 120 ? av.getComentario().substring(0,120) + "..." : av.getComentario() %>"
                </p>
                <% } %>
            </div>
            <% } %>
        </div>
        <% } %>

        <% } /* dbOnline */ %>
    </main>

    <footer>
        <div class="container">
            Livraria · Sistema de Recomendações de Livros por Gênero · UNIUBE ADS MVC
        </div>
    </footer>
</body>
</html>
