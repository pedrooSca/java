package com.livraria;
import com.livraria.config.ConnectionFactory;
import com.livraria.dao.GeneroDAO;
import com.livraria.service.LivroService;
import org.junit.*;
import static org.junit.Assert.*;
/** Opt-in: usa somente banco separado, cujo nome termina em _test. */
public class MySqlIntegrationTest {
    @Test public void crudRealComViewETrigger() throws Exception {
        Assume.assumeTrue("Defina RUN_MYSQL_TESTS=true para executar", "true".equals(System.getenv("RUN_MYSQL_TESTS")));
        String url=System.getenv("DB_URL");
        assertNotNull("DB_URL deve apontar para banco de teste",url);
        assertTrue("Use um banco cujo nome termine em _test",url.matches("jdbc:mysql://[^/]+/[A-Za-z0-9_]+_test(\\?.*)?"));
        var genres=new GeneroDAO().listarTodos();assertFalse("Crie um gênero no banco de teste",genres.isEmpty());
        var service=new LivroService();Integer id=null;
        try {
            var book=service.cadastrar("Teste REST "+System.nanoTime(),"Autor de teste",null,2020,genres.get(0).getId());id=book.getId();
            assertNotNull(id);assertTrue(service.buscarPorId(id).isPresent());
            final int createdId=id;
            assertTrue(service.pesquisar(book.getTitulo(),genres.get(0).getId()).stream().anyMatch(l->l.getLivroId()==createdId));
            assertTrue(service.atualizar(id,"Título atualizado no teste","Autor de teste",null,null,genres.get(0).getId()));
            assertEquals("Título atualizado no teste",service.buscarObrigatorio(id).getTitulo());
            try(var c=ConnectionFactory.getConnection();var ps=c.prepareStatement("SELECT COUNT(*) FROM log_alteracoes_livros WHERE livro_id=? AND acao='UPDATE'")) {
                ps.setInt(1,id);try(var rs=ps.executeQuery()){rs.next();assertTrue(rs.getInt(1)>0);}
            }
            assertTrue(service.excluir(id));assertTrue(service.buscarPorId(id).isEmpty());
        } finally {if(id!=null && service.buscarPorId(id).isPresent())service.excluir(id);}
    }

    @Test public void cadastroPublicoAtomicoComRollback() throws Exception {
        Assume.assumeTrue("Defina RUN_MYSQL_TESTS=true para executar", "true".equals(System.getenv("RUN_MYSQL_TESTS")));
        String url = System.getenv("DB_URL");
        assertNotNull(url);
        assertTrue(url.matches("jdbc:mysql://[^/]+/[A-Za-z0-9_]+_test(\\?.*)?"));
        String novo = "novo-" + System.nanoTime() + "@teste.local";
        String existente = "existente-" + System.nanoTime() + "@teste.local";
        var contas = new com.livraria.dao.ContaAcessoDAO();
        var service = new com.livraria.service.AutenticacaoService(contas,
                new com.livraria.security.JwtService("segredo-jwt-de-teste-com-mais-de-32-bytes"));
        try {
            service.cadastrarLeitor(" Novo Leitor ", novo, "Senha de cadastro!123");
            assertEquals("CONSULTA", service.entrar(novo, "Senha de cadastro!123").orElseThrow().get("perfil"));
            contas.cadastrar(existente, com.livraria.security.Senhas.gerarHash("Senha existente!123"), "ADMIN");
            try {
                service.cadastrarLeitor("Não deve persistir", existente, "Senha de cadastro!123");
                fail("Deveria rejeitar o e-mail já usado na tabela de acesso");
            } catch (java.sql.SQLException esperado) {
                assertTrue(esperado.getSQLState().startsWith("23"));
            }
            try (var conexao = ConnectionFactory.getConnection();
                 var comando = conexao.prepareStatement("SELECT COUNT(*) FROM usuarios WHERE email = ?")) {
                comando.setString(1, existente);
                try (var resultado = comando.executeQuery()) { resultado.next(); assertEquals(0, resultado.getInt(1)); }
            }
            assertTrue(service.entrar(existente, "Senha existente!123").isPresent());
            assertTrue(service.entrar(existente, "Senha de cadastro!123").isEmpty());
        } finally {
            try (var conexao = ConnectionFactory.getConnection()) {
                for (String tabela : new String[]{"contas_acesso", "usuarios"}) {
                    try (var comando = conexao.prepareStatement("DELETE FROM " + tabela + " WHERE email IN (?, ?)")) {
                        comando.setString(1, novo); comando.setString(2, existente); comando.executeUpdate();
                    }
                }
            }
        }
    }
}
