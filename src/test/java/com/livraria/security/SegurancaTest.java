package com.livraria.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.livraria.dao.ContaAcessoDAO;
import com.livraria.model.ContaAcesso;
import com.livraria.service.AutenticacaoService;
import org.junit.Test;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.lang.reflect.Proxy;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.Assert.*;

public class SegurancaTest {
    private static final String SEGREDO = "segredo-de-teste-com-mais-de-32-bytes-123";
    private final JwtService jwt = new JwtService(SEGREDO);

    @Test public void senhaTemSaltERejeitaValorIncorreto() {
        String senha = "Minha senha de teste!";
        String hash = Senhas.gerarHash(senha);
        assertFalse(hash.contains(senha));
        assertNotEquals(hash, Senhas.gerarHash(senha));
        assertTrue(Senhas.verificar(senha, hash));
        assertFalse(Senhas.verificar("outra senha", hash));
        assertFalse(Senhas.verificar(senha, "corrompido"));
    }

    @Test public void loginNaoExpoeHashEAdministradorInicialNaoSobrescreveConta() throws Exception {
        var contas = new ContasMemoria();
        var service = new AutenticacaoService(contas, jwt);
        service.criarAdministradorInicial("admin@teste.local", "Senha inicial forte!");
        String hash = contas.conta.senhaHash();
        service.criarAdministradorInicial("admin@teste.local", "Outra senha forte!");
        assertEquals(hash, contas.conta.senhaHash());
        assertTrue(service.entrar("admin@teste.local", "senha errada").isEmpty());
        assertTrue(service.entrar("outro@teste.local", "Senha inicial forte!").isEmpty());
        var acesso = service.entrar("ADMIN@teste.local", "Senha inicial forte!").orElseThrow();
        assertEquals("ADMIN", acesso.get("perfil"));
        assertFalse(acesso.containsKey("senhaHash"));
        assertEquals("1", jwt.verificar((String) acesso.get("token")).getSubject());
        try { service.cadastrar("outro@teste.local", "Senha inicial forte!", "SUPERADMIN"); fail(); }
        catch (IllegalArgumentException esperado) { assertTrue(esperado.getMessage().contains("Perfil")); }
    }

    @Test public void tokenAssinadoExpiraETokensAdulteradosSaoRejeitados() {
        String token = jwt.emitir(7, "ADMIN");
        var dados = jwt.verificar(token);
        assertEquals("7", dados.getSubject());
        assertEquals(1800, dados.getExpiresAtAsInstant().getEpochSecond() - dados.getIssuedAtAsInstant().getEpochSecond());
        for (String invalido : List.of(token.substring(0, token.lastIndexOf('.') + 1) + "assinatura-alterada",
                emitirComExpiracao(Instant.now().minusSeconds(30)), new JwtService(SEGREDO + "outro").emitir(1, "ADMIN"),
                JWT.create().withSubject("1").withClaim("perfil", "ADMIN").sign(Algorithm.HMAC256(SEGREDO)))) {
            try { jwt.verificar(invalido); fail("Aceitou token inválido"); }
            catch (JWTVerificationException esperado) { /* rejeitado */ }
        }
    }

    @Test public void filtroExigeLoginEAplicaPermissoes() throws Exception {
        assertEquals(401, filtrar("GET", "/livros", null));
        assertEquals(401, filtrar("GET", "/livros", "Bearer invalido"));
        assertEquals(401, filtrar("GET", "/livros", "Bearer " + emitirComExpiracao(Instant.now().minusSeconds(60))));
        assertEquals(200, filtrar("POST", "/auth/login", null));
        assertEquals(200, filtrar("POST", "/auth/cadastro", null));
        assertEquals(401, filtrar("POST", "/auth/contas", null));
        assertEquals(401, filtrar("GET", "/auth/cadastro", null));
        assertEquals(401, filtrar("GET", "/auth/login", null));
        assertEquals(200, filtrar("OPTIONS", "/livros", null));
        String admin = "Bearer " + jwt.emitir(1, "ADMIN");
        String consulta = "Bearer " + jwt.emitir(2, "CONSULTA");
        assertEquals(200, filtrar("DELETE", "/livros/1", admin));
        assertEquals(200, filtrar("GET", "/livros", consulta));
        assertEquals(403, filtrar("POST", "/livros", consulta));
        assertEquals(403, filtrar("PUT", "/usuarios/1/preferencias/2", consulta));
        assertEquals(403, filtrar("GET", "/auditoria", consulta));
        assertEquals(403, filtrar("POST", "/auth/contas", consulta));
    }

    private String emitirComExpiracao(Instant expira) {
        return JWT.create().withIssuer("livraria-api").withAudience("livraria").withSubject("1")
                .withClaim("perfil", "ADMIN").withExpiresAt(expira).sign(Algorithm.HMAC256(SEGREDO));
    }

    private int filtrar(String metodo, String caminho, String header) throws Exception {
        var passou = new AtomicBoolean();
        int[] status = {200};
        var body = new StringWriter();
        HttpServletRequest req = (HttpServletRequest) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{HttpServletRequest.class}, (p,m,a) -> switch(m.getName()) {
            case "getMethod" -> metodo;
            case "getServletPath" -> caminho.startsWith("/auth/") ? "/api/auth" : "/api";
            case "getPathInfo" -> caminho.startsWith("/auth/") ? caminho.substring(5) : caminho;
            case "getRequestURI" -> "/app/api" + caminho;
            case "getContextPath" -> "/app";
            case "getHeader" -> header;
            default -> null;
        });
        HttpServletResponse res = (HttpServletResponse) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{HttpServletResponse.class}, (p,m,a) -> {
            if (m.getName().equals("setStatus")) status[0] = (int)a[0];
            if (m.getName().equals("getWriter")) return new PrintWriter(body);
            return null;
        });
        new AutenticacaoFilter(jwt).doFilter(req, res, (request,response) -> passou.set(true));
        assertEquals(status[0] == 200, passou.get());
        return status[0];
    }

    private static class ContasMemoria extends ContaAcessoDAO {
        private ContaAcesso conta;
        @Override public Optional<ContaAcesso> buscarPorEmail(String email) {
            return conta != null && conta.email().equals(email) ? Optional.of(conta) : Optional.empty();
        }
        @Override public void cadastrar(String email, String hash, String perfil) {
            conta = new ContaAcesso(1, email, hash, perfil);
        }
    }
}
