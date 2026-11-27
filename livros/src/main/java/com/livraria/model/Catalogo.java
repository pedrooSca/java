package com.livraria.model;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/** Dados combinados para a tela do catálogo. */
public record Catalogo(ArrayNode livros, ObjectNode medias) {}
