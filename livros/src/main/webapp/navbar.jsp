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

    String msg = request.getParameter("msg");
    String erro = request.getParameter("erro");
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
                <li><a href="avaliar.jsp" class="nav-link <%= "avaliar".equals(currentPage) ? "active" : "" %>">Avaliações</a></li>
                <li><a href="recomendacoes.jsp" class="nav-link <%= "recomendacoes".equals(currentPage) ? "active" : "" %>">Recomendações</a></li>
                <li><a href="usuarios.jsp" class="nav-link <%= "usuarios".equals(currentPage) ? "active" : "" %>">Usuários</a></li>
                <li><a href="auditoria.jsp" class="nav-link <%= "auditoria".equals(currentPage) ? "active" : "" %>">Auditoria</a></li>
            </ul>
        </nav>
        <div class="nav-actions">
            <a href="novo-livro.jsp" class="btn btn-primary btn-sm">
                <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line></svg>
                Novo Livro
            </a>
        </div>
    </div>
</header>

<div class="container" style="margin-top: 22px;">
<% if (msg != null && !msg.isEmpty()) { 
    String textoMsg = msg;
    if ("livro_criado".equals(msg)) textoMsg = "Livro cadastrado com sucesso no catálogo!";
    else if ("livro_atualizado".equals(msg)) textoMsg = "Livro atualizado com sucesso! A trigger gravou o evento no log de auditoria.";
    else if ("livro_excluido".equals(msg)) textoMsg = "Livro excluído com sucesso! A trigger registrou a exclusão no log.";
    else if ("avaliacao_criada".equals(msg)) textoMsg = "Avaliação registrada com sucesso! Obrigado pela sua opinião.";
    else if ("preferencia_atualizada".equals(msg)) textoMsg = "Preferências literárias do usuário atualizadas com sucesso!";
    else if ("usuario_criado".equals(msg)) textoMsg = "Novo usuário cadastrado com sucesso!";
%>
    <div class="alert alert-success">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"></path><polyline points="22 4 12 14.01 9 11.01"></polyline></svg>
        <div><strong>Sucesso!</strong> <%= textoMsg %></div>
    </div>
<% } %>

<% if (erro != null && !erro.isEmpty()) { %>
    <div class="alert alert-error">
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg>
        <div><strong>Atenção:</strong> <%= erro %></div>
    </div>
<% } %>
</div>
