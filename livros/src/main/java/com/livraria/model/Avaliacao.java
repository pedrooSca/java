package com.livraria.model;

import java.sql.Timestamp;

/**
 * Modelo representativo de uma avaliação de livro feita por um usuário.
 */
public class Avaliacao {

    private Integer id;
    private int usuarioId;
    private String usuarioNome;
    private int livroId;
    private String livroTitulo;
    private int nota;
    private String comentario;
    private Timestamp dataAvaliacao;

    public Avaliacao() {
    }

    public Avaliacao(int usuarioId, int livroId, int nota, String comentario) {
        this.usuarioId = usuarioId;
        this.livroId = livroId;
        this.nota = nota;
        this.comentario = comentario;
    }

    public Avaliacao(Integer id, int usuarioId, int livroId, int nota, String comentario, Timestamp dataAvaliacao) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.livroId = livroId;
        this.nota = nota;
        this.comentario = comentario;
        this.dataAvaliacao = dataAvaliacao;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getUsuarioNome() {
        return usuarioNome;
    }

    public void setUsuarioNome(String usuarioNome) {
        this.usuarioNome = usuarioNome;
    }

    public int getLivroId() {
        return livroId;
    }

    public void setLivroId(int livroId) {
        this.livroId = livroId;
    }

    public String getLivroTitulo() {
        return livroTitulo;
    }

    public void setLivroTitulo(String livroTitulo) {
        this.livroTitulo = livroTitulo;
    }

    public int getNota() {
        return nota;
    }

    public void setNota(int nota) {
        this.nota = nota;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public Timestamp getDataAvaliacao() {
        return dataAvaliacao;
    }

    public void setDataAvaliacao(Timestamp dataAvaliacao) {
        this.dataAvaliacao = dataAvaliacao;
    }

    @Override
    public String toString() {
        return "Avaliacao{" +
                "id=" + id +
                ", usuarioId=" + usuarioId +
                ", usuarioNome='" + usuarioNome + '\'' +
                ", livroId=" + livroId +
                ", livroTitulo='" + livroTitulo + '\'' +
                ", nota=" + nota +
                ", comentario='" + comentario + '\'' +
                ", dataAvaliacao=" + dataAvaliacao +
                '}';
    }
}
