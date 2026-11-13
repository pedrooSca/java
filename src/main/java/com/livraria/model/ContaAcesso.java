package com.livraria.model;

/** Credenciais ficam restritas ao DAO e ao serviço; nunca são serializadas na API. */
public record ContaAcesso(int id, String email, String senhaHash, String perfil) {}
