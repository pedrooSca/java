<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Entrar | Livraria</title>
    <link rel="stylesheet" href="css/style.css?v=2">
</head>
<body>
<main class="container" style="max-width:460px;padding-top:60px">
    <h1>Entrar na Livraria</h1>
    <p>Use sua conta de acesso para consultar o acervo.</p>
    <div id="api-message" role="status" aria-live="polite"></div>
    <div id="api-content" data-page="login">
        <form id="login-form" class="card">
            <div class="form-group"><label for="email" class="form-label">E-mail</label>
                <input id="email" name="email" type="email" class="form-control" autocomplete="username" maxlength="100" required></div>
            <div class="form-group"><label for="senha" class="form-label">Senha</label>
                <input id="senha" name="senha" type="password" class="form-control" autocomplete="current-password" maxlength="200" required></div>
            <button type="submit" class="btn btn-primary">Entrar</button>
        </form>
        <details class="card" style="margin-top:20px">
            <summary style="cursor:pointer;font-weight:600">Ainda não tem cadastro? Criar usuário</summary>
            <h2 style="margin-top:16px">Cadastrar usuário</h2>
            <p>Crie seu usuário e uma conta de consulta para entrar na Livraria.</p>
            <form id="user-form">
                <div class="form-group"><label for="cadastro-nome" class="form-label">Nome completo</label>
                    <input id="cadastro-nome" name="nome" class="form-control" autocomplete="name" maxlength="100" required></div>
                <div class="form-group"><label for="cadastro-email" class="form-label">E-mail</label>
                    <input id="cadastro-email" name="email" type="email" class="form-control" autocomplete="email" maxlength="100" required></div>
                <div class="form-group"><label for="cadastro-senha" class="form-label">Senha (mínimo de 12 caracteres)</label>
                    <input id="cadastro-senha" name="senha" type="password" class="form-control" autocomplete="new-password" minlength="12" maxlength="200" required></div>
                <button type="submit" class="btn btn-primary">Cadastrar usuário</button>
            </form>
        </details>
    </div>
    <noscript>Ative JavaScript para entrar no sistema.</noscript>
</main>
<script src="js/app.js?v=3" defer></script>
</body>
</html>
