package com.livraria.service;

/** Recurso solicitado inexistente; o controller HTTP traduz para 404. */
public class RecursoNaoEncontradoException extends IllegalArgumentException {
  public RecursoNaoEncontradoException(String mensagem) {
    super(mensagem);
  }
}
