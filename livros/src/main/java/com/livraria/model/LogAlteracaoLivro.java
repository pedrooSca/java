package com.livraria.model;

import java.sql.Timestamp;

/**
 * Entidade que mapeia a tabela de auditoria 'log_alteracoes_livros',
 * alimentada automaticamente pelos Triggers de UPDATE e DELETE.
 */
public class LogAlteracaoLivro {
    private Integer id;
    private Integer livroId;
    private String acao;
    private String tituloAnterior;
    private String tituloNovo;
    private Timestamp dataAlteracao;

    public LogAlteracaoLivro() {
    }

    public LogAlteracaoLivro(Integer id, Integer livroId, String acao, String tituloAnterior, String tituloNovo, Timestamp dataAlteracao) {
        this.id = id;
        this.livroId = livroId;
        this.acao = acao;
        this.tituloAnterior = tituloAnterior;
        this.tituloNovo = tituloNovo;
        this.dataAlteracao = dataAlteracao;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getLivroId() {
        return livroId;
    }

    public void setLivroId(Integer livroId) {
        this.livroId = livroId;
    }

    public String getAcao() {
        return acao;
    }

    public void setAcao(String acao) {
        this.acao = acao;
    }

    public String getTituloAnterior() {
        return tituloAnterior;
    }

    public void setTituloAnterior(String tituloAnterior) {
        this.tituloAnterior = tituloAnterior;
    }

    public String getTituloNovo() {
        return tituloNovo;
    }

    public void setTituloNovo(String tituloNovo) {
        this.tituloNovo = tituloNovo;
    }

    public Timestamp getDataAlteracao() {
        return dataAlteracao;
    }

    public void setDataAlteracao(Timestamp dataAlteracao) {
        this.dataAlteracao = dataAlteracao;
    }

    @Override
    public String toString() {
        return String.format("[Log #%d] Ação: %-6s | Livro ID: %-3d | De: \"%s\" -> Para: \"%s\" | Em: %s",
                id,
                acao,
                livroId,
                tituloAnterior != null ? tituloAnterior : "-",
                tituloNovo != null ? tituloNovo : "-",
                dataAlteracao != null ? dataAlteracao.toString() : "-");
    }
}
