package com.livraria.model;

import java.net.http.HttpHeaders;

public record RespostaApi(int status, HttpHeaders cabecalhos, byte[] corpo) {}
