<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.livraria.config.ConnectionFactory" %>
<%@ page import="com.livraria.dao.LogAuditoriaDAO" %>
<%@ page import="com.livraria.model.LogAlteracaoLivro" %>
<%@ page import="java.util.List" %>
<%
    boolean dbOnline = false;
    List<LogAlteracaoLivro> logs = null;
    int totalLogs = 0;

    try {
        dbOnline = ConnectionFactory.testarConexao();
        if (dbOnline) {
            logs = new LogAuditoriaDAO().listarLogs();
            totalLogs = logs != null ? logs.size() : 0;
        }
    } catch (Exception e) {
        dbOnline = false;
    }
%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Auditoria — Log de Triggers | Livraria</title>
    <link rel="stylesheet" href="css/style.css">
</head>
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

        <% if (!dbOnline) { %>
            <div class="alert alert-error">O banco de dados está temporariamente inacessível.</div>
        <% } else { %>

        <% if (totalLogs == 0) { %>
            <div class="alert alert-info">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="16" x2="12" y2="12"></line><line x1="12" y1="8" x2="12.01" y2="8"></line></svg>
                <div>Nenhum evento de auditoria registrado ainda. Edite ou exclua um livro no catálogo para ver os triggers em ação.</div>
            </div>
        <% } %>

        <!-- Cartões de métricas de auditoria -->
        <div class="stats-grid" style="margin-bottom: 30px;">
            <% 
                int countUpdate = 0;
                int countDelete = 0;
                if (logs != null) {
                    for (LogAlteracaoLivro l : logs) {
                        if ("UPDATE".equalsIgnoreCase(l.getAcao())) countUpdate++;
                        if ("DELETE".equalsIgnoreCase(l.getAcao())) countDelete++;
                    }
                }
            %>
            <div class="stat-card">
                <div class="stat-value"><%= totalLogs %></div>
                <div class="stat-label">Eventos Registrados</div>
            </div>
            <div class="stat-card">
                <div class="stat-value" style="color: #1e40af;"><%= countUpdate %></div>
                <div class="stat-label">Atualizações (UPDATE)</div>
            </div>
            <div class="stat-card">
                <div class="stat-value" style="color: var(--danger);"><%= countDelete %></div>
                <div class="stat-label">Exclusões (DELETE)</div>
            </div>
        </div>

        <!-- Alerta explicativo sobre Triggers -->
        <div class="alert alert-warning" style="margin-bottom: 24px;">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"></path><line x1="12" y1="9" x2="12" y2="13"></line><line x1="12" y1="17" x2="12.01" y2="17"></line></svg>
            <div>
                <strong>Como funciona:</strong> Os triggers são disparados automaticamente pelo MySQL sempre que uma linha da tabela <code>livros</code> é alterada (UPDATE) ou removida (DELETE). Os registros abaixo foram inseridos por esses triggers, sem intervenção direta da aplicação.
            </div>
        </div>

        <!-- Tabela de Logs -->
        <div class="table-responsive">
            <table class="data-table">
                <thead>
                    <tr>
                        <th>#</th>
                        <th>Ação</th>
                        <th>ID do Livro</th>
                        <th>Título Anterior</th>
                        <th>Título Novo</th>
                        <th>Data / Hora</th>
                    </tr>
                </thead>
                <tbody>
                <% if (logs != null && !logs.isEmpty()) {
                    for (LogAlteracaoLivro log : logs) { 
                        String badgeClass = "UPDATE".equalsIgnoreCase(log.getAcao()) ? "badge-update" : "badge-delete";
                %>
                    <tr>
                        <td><strong><%= log.getId() %></strong></td>
                        <td><span class="badge-action <%= badgeClass %>"><%= log.getAcao() %></span></td>
                        <td>#<%= log.getLivroId() %></td>
                        <td><%= log.getTituloAnterior() != null ? log.getTituloAnterior() : "—" %></td>
                        <td><%= log.getTituloNovo() != null ? log.getTituloNovo() : "<em style='color:var(--muted)'>Livro removido</em>" %></td>
                        <td style="white-space: nowrap; color: var(--muted);">
                            <%= log.getDataAlteracao() != null ? new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(log.getDataAlteracao()) : "—" %>
                        </td>
                    </tr>
                <% } } else { %>
                    <tr>
                        <td colspan="6" style="text-align: center; padding: 40px 20px; color: var(--muted);">
                            Nenhum evento de auditoria para exibir.
                        </td>
                    </tr>
                <% } %>
                </tbody>
            </table>
        </div>

        <% } %>
    </main>

    <footer>
        <div class="container">
            Livraria · Sistema de Recomendações de Livros por Gênero · UNIUBE ADS MVC
        </div>
    </footer>
</body>
</html>
