package com.livraria.controller;

import com.fasterxml.jackson.databind.*;
import com.livraria.dao.*;
import com.livraria.model.*;
import com.livraria.service.*;
import org.junit.*;
import javax.servlet.http.*;
import java.io.*;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.*;
import static org.junit.Assert.*;

/** Integra o Controller HTTP com o Service real e DAOs em memória, sem MySQL. */
public class ApiServletTest {
    private final ObjectMapper json = new ObjectMapper();
    private MemoryDAO dao;
    private ApiServlet api;
    @Before public void setup() {
        dao = new MemoryDAO();
        api = new ApiServlet(new LivroService(dao, new GeneroDAO() {
            @Override public Optional<Genero> buscarPorId(int id) { return id==1 ? Optional.of(new Genero(1,"Fantasia","")) : Optional.empty(); }
        })) { @Override public void log(String message, Throwable t) { /* não usa ServletContext em testes */ } };
    }
    private Result request(String method,String path,String body,String type,Map<String,String> query) throws Exception {
        HttpServletRequest req=(HttpServletRequest)Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{HttpServletRequest.class},(p,m,a)->{
            switch(m.getName()) {
                case "getMethod":return method;
                case "getPathInfo":return path;
                case "getContentType":return type;
                case "getContextPath":return "/livros";
                case "getReader":return new BufferedReader(new StringReader(body==null ? "" : body));
                case "getParameter":return query.get((String)a[0]);
                default:return null;
            }
        });
        Result result=new Result();
        HttpServletResponse res=(HttpServletResponse)Proxy.newProxyInstance(getClass().getClassLoader(),new Class[]{HttpServletResponse.class},(p,m,a)->{
            switch(m.getName()) {
                case "setStatus":result.status=(int)a[0];return null;
                case "setHeader":result.headers.put((String)a[0],(String)a[1]);return null;
                case "getWriter":return new PrintWriter(result.body);
                default:return null;
            }
        });
        api.service(req,res);return result;
    }
    private Result call(String method,String path,String body) throws Exception {return request(method,path,body,"application/json",Map.of());}
    private String valid() {return "{\"titulo\":\" Duna \",\"autor\":\"Frank Herbert\",\"isbn\":null,\"anoPublicacao\":1965,\"generoId\":1}";}
    @Test public void crudCompletoComLocationEFiltros() throws Exception {
        Result created=call("POST","/livros",valid()); assertEquals(201,created.status);
        assertEquals("/livros/api/livros/1",created.headers.get("Location"));
        assertEquals("Duna",json.readTree(created.body.toString()).get("titulo").asText());
        Result listed=request("GET","/livros",null,null,Map.of("q","Duna","generoId","1"));
        assertEquals(200,listed.status); assertEquals("Duna",dao.termo);assertEquals(Integer.valueOf(1),dao.genero);
        assertEquals(1,json.readTree(listed.body.toString()).size());
        dao.data.get(1).setDataCadastro(java.sql.Timestamp.valueOf("2026-10-28 12:00:00"));
        Result found=call("GET","/livros/1",null);assertEquals(200,found.status);
        assertTrue(json.readTree(found.body.toString()).get("dataCadastro").isTextual());
        assertEquals(200,call("PUT","/livros/1",valid().replace(" Duna ","Duna revisado")).status);
        assertEquals("Duna revisado",dao.data.get(1).getTitulo());
        Result deleted=call("DELETE","/livros/1",null);assertEquals(204,deleted.status);assertEquals("",deleted.body.toString());
        assertEquals(404,call("GET","/livros/1",null).status);
        assertEquals(404,call("DELETE","/livros/1",null).status);
        assertEquals(404,call("PUT","/livros/1",valid()).status);
    }
    @Test public void validaDadosAntesDePersistir() throws Exception {
        for(String body:List.of(valid().replace(" Duna "," "),valid().replace("Frank Herbert",""),valid().replace("1965","3500"),valid().replace("1965","-1"),valid().replace("\"generoId\":1","\"generoId\":99"),valid().replace(" Duna ","x".repeat(151)),valid().replace("Frank Herbert","x".repeat(101)),valid().replace("\"isbn\":null","\"isbn\":\""+"x".repeat(21)+"\""))) {
            assertEquals(body,400,call("POST","/livros",body).status);
        }
        assertTrue(dao.data.isEmpty());
    }
    @Test public void validaIsbnTambemNaAtualizacao() throws Exception {
        call("POST","/livros",valid());
        assertEquals(400,call("PUT","/livros/1",valid().replace("\"isbn\":null","\"isbn\":\""+"x".repeat(21)+"\"")).status);
        assertNull(dao.data.get(1).getIsbn());
    }
    @Test public void rejeitaJsonMalformadoETiposIncorretos() throws Exception {
        for(String body:List.of("", "null", "[]", "{", valid()+"{}",valid().replace("1965","1965.5"),valid().replace("1965","\"1965\""),valid().replace("\"generoId\":1","\"generoId\":null"),valid().replace("\"generoId\":1","\"generoId\":2147483648"),valid().replace("\"generoId\":1","\"generoId\":0"),valid().replace("\" Duna \"","{}")))
            assertEquals(body,400,call("POST","/livros",body).status);
    }
    @Test public void aceitaCamposOpcionaisNulos() throws Exception {
        Result result=call("POST","/livros","{\"titulo\":\"Título\",\"autor\":\"Autor\",\"generoId\":1}");
        assertEquals(201,result.status);assertNull(dao.data.get(1).getAnoPublicacao());
    }
    @Test public void errosDeProtocolo() throws Exception {
        assertEquals(415,request("POST","/livros",valid(),"text/plain",Map.of()).status);
        assertEquals(413,call("POST","/livros"," ".repeat(65537)).status);
        for(String id:List.of("abc","0","-1","999999999999")) assertEquals(400,call("GET","/livros/"+id,null).status);
        assertEquals(400,request("GET","/livros",null,null,Map.of("generoId","abc")).status);
        assertEquals(404,call("GET","/inexistente",null).status);
        Result wrong=call("PATCH","/livros/1",valid());assertEquals(405,wrong.status);assertTrue(wrong.headers.get("Allow").contains("PUT"));
        assertEquals(204,call("OPTIONS","/livros",null).status);
    }
    @Test public void falhaDeBancoNaoVazaDetalhes() throws Exception {
        dao.failure=new SQLException("senha e SQL internos","08001");
        Result result=call("POST","/livros",valid());assertEquals(503,result.status);
        assertFalse(result.body.toString().contains("senha"));assertFalse(result.body.toString().contains("08001"));
        dao.failure=new SQLException("duplicidade interna","23000");assertEquals(409,call("POST","/livros",valid()).status);
    }
    static class Result { int status=200;StringWriter body=new StringWriter();Map<String,String> headers=new HashMap<>(); }
    static class MemoryDAO extends LivroDAO {
        Map<Integer,Livro> data=new LinkedHashMap<>(); SQLException failure;String termo;Integer genero;
        @Override public Livro inserir(Livro l) throws SQLException {if(failure!=null)throw failure;l.setId(data.size()+1);data.put(l.getId(),l);return l;}
        @Override public Optional<Livro> buscarPorId(int id){return Optional.ofNullable(data.get(id));}
        @Override public boolean atualizar(Livro l){if(!data.containsKey(l.getId()))return false;data.put(l.getId(),l);return true;}
        @Override public boolean excluir(int id){return data.remove(id)!=null;}
        @Override public List<LivroDetalhadoDTO> pesquisar(String q,Integer g){termo=q;genero=g;return data.values().stream().map(l->new LivroDetalhadoDTO(l.getId(),l.getTitulo(),l.getAutor(),l.getIsbn(),l.getAnoPublicacao(),l.getGeneroId(),"Fantasia")).toList();}
    }
}
