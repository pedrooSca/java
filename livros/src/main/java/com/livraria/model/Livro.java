package com.livraria.model;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * Entidade que representa a tabela 'livros' (Alvo principal do CRUD).
 */
public class Livro {
    private Integer id;
    private String titulo;
    private String autor;
    private String isbn;
    private Integer anoPublicacao;
    private Integer generoId;
    private Timestamp dataCadastro;

    public Livro() {
    }

    public Livro(String titulo, String autor, String isbn, Integer anoPublicacao, Integer generoId) {
        this.titulo = titulo;
        this.autor = autor;
        this.isbn = isbn;
        this.anoPublicacao = anoPublicacao;
        this.generoId = generoId;
    }

    public Livro(Integer id, String titulo, String autor, String isbn, Integer anoPublicacao, Integer generoId) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
        this.isbn = isbn;
        this.anoPublicacao = anoPublicacao;
        this.generoId = generoId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public Integer getAnoPublicacao() {
        return anoPublicacao;
    }

    public void setAnoPublicacao(Integer anoPublicacao) {
        this.anoPublicacao = anoPublicacao;
    }

    public Integer getGeneroId() {
        return generoId;
    }

    public void setGeneroId(Integer generoId) {
        this.generoId = generoId;
    }

    public Timestamp getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(Timestamp dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    @Override
    public String toString() {
        return String.format("ID: %d | \"%s\" por %s (%s) - Gênero ID: %d",
                id != null ? id : 0, titulo, autor,
                anoPublicacao != null ? anoPublicacao : "N/D",
                generoId != null ? generoId : 0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Livro)) return false;
        Livro livro = (Livro) o;
        return Objects.equals(id, livro.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
