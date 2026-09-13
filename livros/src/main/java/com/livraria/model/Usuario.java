package com.livraria.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entidade que representa a tabela 'usuarios'.
 */
public class Usuario {
    private Integer id;
    private String nome;
    private String email;
    private Timestamp dataCadastro;
    private List<Genero> generosPreferidos = new ArrayList<>();

    public Usuario() {
    }

    public Usuario(Integer id, String nome, String email) {
        this.id = id;
        this.nome = nome;
        this.email = email;
    }

    public Usuario(String nome, String email) {
        this.nome = nome;
        this.email = email;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Timestamp getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(Timestamp dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public List<Genero> getGenerosPreferidos() {
        return generosPreferidos;
    }

    public void setGenerosPreferidos(List<Genero> generosPreferidos) {
        this.generosPreferidos = generosPreferidos;
    }

    @Override
    public String toString() {
        return "ID: " + id + " | " + nome + " <" + email + ">";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Usuario)) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(id, usuario.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
