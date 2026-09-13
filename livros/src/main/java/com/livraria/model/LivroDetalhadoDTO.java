package com.livraria.model;

/**
 * Data Transfer Object (DTO) para representar a VIEW 'vw_livros_detalhados'
 * e os resultados da Stored Procedure 'sp_obter_recomendacoes_usuario'.
 */
public class LivroDetalhadoDTO {
    private Integer livroId;
    private String titulo;
    private String autor;
    private String isbn;
    private Integer anoPublicacao;
    private Integer generoId;
    private String generoNome;

    public LivroDetalhadoDTO() {
    }

    public LivroDetalhadoDTO(Integer livroId, String titulo, String autor, String isbn, Integer anoPublicacao, Integer generoId, String generoNome) {
        this.livroId = livroId;
        this.titulo = titulo;
        this.autor = autor;
        this.isbn = isbn;
        this.anoPublicacao = anoPublicacao;
        this.generoId = generoId;
        this.generoNome = generoNome;
    }

    public Integer getLivroId() {
        return livroId;
    }

    public void setLivroId(Integer livroId) {
        this.livroId = livroId;
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

    public String getGeneroNome() {
        return generoNome;
    }

    public void setGeneroNome(String generoNome) {
        this.generoNome = generoNome;
    }

    @Override
    public String toString() {
        return String.format("[%d] %-30s | %-20s | Ano: %-4s | Gênero: %s",
                livroId,
                titulo,
                autor,
                anoPublicacao != null ? anoPublicacao.toString() : "N/D",
                generoNome != null ? generoNome : "Sem gênero");
    }
}
