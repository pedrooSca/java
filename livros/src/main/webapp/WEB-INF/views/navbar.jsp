<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    String currentPage = request.getParameter("activePage");
    if (currentPage == null) {
        String uri = request.getRequestURI();
        if (uri.endsWith("index.jsp") || uri.endsWith("/")) currentPage = "index";
        else if (uri.endsWith("livros.jsp")) currentPage = "livros";
        else if (uri.endsWith("novo-livro.jsp")) currentPage = "novo";
        else if (uri.endsWith("avaliar.jsp")) currentPage = "avaliar";
        else if (uri.endsWith("recomendacoes.jsp")) currentPage = "recomendacoes";
        else if (uri.endsWith("usuarios.jsp")) currentPage = "usuarios";
        else if (uri.endsWith("auditoria.jsp")) currentPage = "auditoria";
        else currentPage = "";
    }



%>
<header class="nav-header">
    <div class="container nav-container">
        <a href="index.jsp" class="nav-brand">
            <svg width="26" height="26" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
                <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
            </svg>
            <span>Livraria</span>
            <span class="tagline">MVC · MySQL</span>
        </a>
        <nav>
            <ul class="nav-menu">
                <li><a href="index.jsp" class="nav-link <%= "index".equals(currentPage) ? "active" : "" %>">Início</a></li>
                <li><a href="livros" class="nav-link <%= "livros".equals(currentPage) ? "active" : "" %>">Catálogo & Pesquisa</a></li>
                <li><a href="avaliacoes" class="nav-link <%= "avaliar".equals(currentPage) ? "active" : "" %>">Avaliações</a></li>
                <li><a href="recomendacoes" class="nav-link <%= "recomendacoes".equals(currentPage) ? "active" : "" %>">Recomendações</a></li>
                <li><a href="usuarios" class="nav-link <%= "usuarios".equals(currentPage) ? "active" : "" %>">Usuários</a></li>
                <li><a href="auditoria" class="nav-link <%= "auditoria".equals(currentPage) ? "active" : "" %>">Auditoria</a></li>
            </ul>
        </nav>
        <div class="nav-actions">
            <span id="account-profile"></span>
            <button id="logout" type="button" class="btn btn-secondary btn-sm">Sair</button>
            <a data-admin href="novo-livro" class="btn btn-primary btn-sm">
                <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line></svg>
                Novo Livro
            </a>
        </div>
    </div>
</header>
