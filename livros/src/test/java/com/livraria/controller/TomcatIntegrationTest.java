package com.livraria.controller;

import com.sun.net.httpserver.HttpServer;
import org.apache.catalina.startup.Tomcat;
import org.junit.Test;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.Assert.*;

/** Testa as JSPs e a ligação HTTP com uma API em outro servidor. */
public class TomcatIntegrationTest {
    @Test public void telasEProxySeparadosDaApi() throws Exception {
        var received = new AtomicReference<String>();
        var authorization = new AtomicReference<String>();
        var api = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        api.createContext("/api/", exchange -> {
            authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            received.set(exchange.getRequestMethod() + " " + exchange.getRequestURI() + " " + body);
            int status = switch (exchange.getRequestMethod()) {
                case "POST" -> 201;
                case "DELETE", "OPTIONS" -> 204;
                default -> exchange.getRequestURI().getPath().endsWith("/999") ? 404 : 200;
            };
            exchange.getResponseHeaders().set("Content-Type", "application/json;charset=UTF-8");
            exchange.getResponseHeaders().set("Location", "/api/livros/1");
            exchange.getResponseHeaders().set("Allow", "GET, POST, PUT, DELETE, OPTIONS");
            byte[] response = (status == 404 ? "{\"erro\":\"Livro não encontrado\"}" : "{\"titulo\":\"Ação\"}").getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, status == 204 ? -1 : response.length);
            if (status != 204) exchange.getResponseBody().write(response);
            exchange.close();
        });
        api.start();
        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir(Files.createTempDirectory(Path.of("target"), "tomcat-test-").toAbsolutePath().toString());
        tomcat.setPort(0);
        tomcat.getConnector();
        var context = tomcat.addWebapp("/livros", Path.of("src/main/webapp").toAbsolutePath().toString());
        context.setParentClassLoader(getClass().getClassLoader());
        var proxy = Tomcat.addServlet(context, "proxy-test", new ApiProxyServlet());
        proxy.addInitParameter("apiUrl", "http://localhost:" + api.getAddress().getPort());
        context.addServletMappingDecoded("/api/*", "proxy-test");
        Tomcat.addServlet(context, "pages-test", new PaginaServlet());
        for (String route : new String[]{"home", "livros", "novo-livro", "editar-livro", "avaliacoes", "recomendacoes", "usuarios", "auditoria"}) {
            context.addServletMappingDecoded("/" + route, "pages-test");
        }
        try {
            tomcat.start();
            String base = "http://localhost:" + tomcat.getConnector().getLocalPort() + "/livros";
            var client = HttpClient.newHttpClient();
            for (String page : new String[]{"login", "index", "livros", "novo-livro", "editar-livro", "usuarios", "avaliar", "recomendacoes", "auditoria"}) {
                var response = client.send(HttpRequest.newBuilder(URI.create(base + "/" + page + ".jsp")).GET().build(), HttpResponse.BodyHandlers.ofString());
                assertEquals(page, 200, response.statusCode());
                assertTrue(response.body().contains("data-page=\"" + page + "\""));
                assertFalse(response.body().contains("<%"));
            }
            var route = client.send(HttpRequest.newBuilder(URI.create(base + "/livros")).GET().build(), HttpResponse.BodyHandlers.ofString());
            assertEquals(200, route.statusCode());
            assertTrue(route.body().contains("data-page=\"livros\""));
            for (String method : new String[]{"GET", "POST", "PUT", "DELETE", "OPTIONS"}) {
                String body = method.equals("POST") || method.equals("PUT") ? "{\"titulo\":\"Ação\"}" : "";
                var response = client.send(HttpRequest.newBuilder(URI.create(base + "/api/livros?q=fic%C3%A7%C3%A3o"))
                        .header("Content-Type", "application/json").header("Authorization", "Bearer token-teste")
                        .method(method, HttpRequest.BodyPublishers.ofString(body)).build(), HttpResponse.BodyHandlers.ofString());
                assertEquals("Bearer token-teste", authorization.get());
                assertEquals(method + " /api/livros?q=fic%C3%A7%C3%A3o " + body, received.get());
                assertEquals(method.equals("POST") ? 201 : method.equals("DELETE") || method.equals("OPTIONS") ? 204 : 200, response.statusCode());
                assertEquals("/livros/api/livros/1", response.headers().firstValue("Location").orElseThrow());
                assertTrue(response.headers().firstValue("Allow").orElseThrow().contains("PUT"));
                if (response.statusCode() != 204) assertTrue(response.body().contains("Ação"));
                else assertEquals("", response.body());
            }
            var missing = client.send(HttpRequest.newBuilder(URI.create(base + "/api/livros/999")).GET().build(), HttpResponse.BodyHandlers.ofString());
            assertEquals(404, missing.statusCode());
            assertTrue(missing.body().contains("Livro não encontrado"));
            var oversized = client.send(HttpRequest.newBuilder(URI.create(base + "/api/livros"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("x".repeat(262145))).build(), HttpResponse.BodyHandlers.ofString());
            assertEquals(413, oversized.statusCode());
            assertTrue(oversized.body().contains("Requisição muito grande"));
            api.stop(0);
            var unavailable = client.send(HttpRequest.newBuilder(URI.create(base + "/api/livros")).GET().build(), HttpResponse.BodyHandlers.ofString());
            assertEquals(502, unavailable.statusCode());
            assertTrue(unavailable.body().contains("API indisponível"));
        } finally {
            api.stop(0);
            tomcat.stop();
            tomcat.destroy();
        }
    }
}
