package com.livraria.model;

import java.util.Map;

public record RequisicaoApi(
    String metodo, String caminho, String query, Map<String, String> cabecalhos, byte[] corpo) {}
