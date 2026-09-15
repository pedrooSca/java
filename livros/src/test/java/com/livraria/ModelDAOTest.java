package com.livraria;

import com.livraria.config.ConnectionFactory;
import com.livraria.controller.LivroController;
import com.livraria.model.Genero;
import com.livraria.model.Livro;
import com.livraria.model.LivroDetalhadoDTO;
import com.livraria.model.Usuario;
import org.junit.Test;

import static org.junit.Assert.*;

public class ModelDAOTest {

    @Test
    public void testGeneroModel() {
        Genero g = new Genero(1, "Ficção Científica", "Histórias de ficção");
        assertEquals(Integer.valueOf(1), g.getId());
        assertEquals("Ficção Científica", g.getNome());
        assertTrue(g.toString().contains("Ficção Científica"));
    }

    @Test
    public void testUsuarioModel() {
        Usuario u = new Usuario(1, "Aluno ADS UNIUBE", "aluno.ads@uniube.br");
        assertEquals("Aluno ADS UNIUBE", u.getNome());
        assertEquals("aluno.ads@uniube.br", u.getEmail());
    }

    @Test
    public void testLivroModel() {
        Livro livro = new Livro(10, "Duna", "Frank Herbert", "978-8576572008", 1965, 1);
        assertEquals("Duna", livro.getTitulo());
        assertEquals("Frank Herbert", livro.getAutor());
        assertEquals(Integer.valueOf(1965), livro.getAnoPublicacao());
        assertEquals(Integer.valueOf(1), livro.getGeneroId());
    }

    @Test
    public void testLivroDetalhadoDTO() {
        LivroDetalhadoDTO dto = new LivroDetalhadoDTO(1, "Duna", "Frank Herbert", "978-8576572008", 1965, 1, "Ficção Científica");
        assertEquals("Ficção Científica", dto.getGeneroNome());
        assertTrue(dto.toString().contains("Duna"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidacaoLivroTituloVazio() {
        LivroController controller = new LivroController(null, null);
        try {
            controller.cadastrarLivro("", "Autor Teste", "123", 2020, 1);
        } catch (java.sql.SQLException e) {
            fail("Deveria lançar IllegalArgumentException antes do banco");
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testValidacaoLivroAnoInvalido() {
        LivroController controller = new LivroController(null, null);
        try {
            controller.cadastrarLivro("Titulo", "Autor Teste", "123", 3500, 1);
        } catch (java.sql.SQLException e) {
            fail("Deveria lançar IllegalArgumentException para ano no futuro distante");
        }
    }

    @Test
    public void testAvaliacaoModel() {
        com.livraria.model.Avaliacao av = new com.livraria.model.Avaliacao(1, 2, 5, "Excelente leitura!");
        av.setId(10);
        av.setUsuarioNome("Maria Oliveira");
        av.setLivroTitulo("Duna");

        assertEquals(Integer.valueOf(10), av.getId());
        assertEquals(1, av.getUsuarioId());
        assertEquals("Maria Oliveira", av.getUsuarioNome());
        assertEquals(2, av.getLivroId());
        assertEquals("Duna", av.getLivroTitulo());
        assertEquals(5, av.getNota());
        assertEquals("Excelente leitura!", av.getComentario());
        assertTrue(av.toString().contains("Excelente leitura!"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAvaliacaoNotaInvalida() throws java.sql.SQLException {
        com.livraria.dao.AvaliacaoDAO dao = new com.livraria.dao.AvaliacaoDAO();
        com.livraria.model.Avaliacao av = new com.livraria.model.Avaliacao(1, 1, 6, "Nota maior que 5");
        dao.inserir(av);
    }

    @Test
    public void testConnectionFactoryConfigCarregada() {
        assertNotNull(ConnectionFactory.getUrl());
        assertNotNull(ConnectionFactory.getUser());
        assertTrue(ConnectionFactory.getUrl().contains("db_recomendador_livros"));
    }
}
