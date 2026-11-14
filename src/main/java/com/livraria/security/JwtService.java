package com.livraria.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

/** Tokens assinados duram 30 minutos; a API não cria sessão HTTP. */
public class JwtService {
  private static final String EMISSOR = "livraria-api";
  private final Algorithm algoritmo;
  private final JWTVerifier verificador;

  public JwtService(String segredo) {
    if (segredo == null || segredo.getBytes(StandardCharsets.UTF_8).length < 32) {
      throw new IllegalArgumentException("Defina JWT_SECRET com pelo menos 32 bytes.");
    }
    algoritmo = Algorithm.HMAC256(segredo);
    verificador =
        JWT.require(algoritmo)
            .withIssuer(EMISSOR)
            .withAudience("livraria")
            .withClaimPresence("exp")
            .withClaimPresence("sub")
            .withClaimPresence("perfil")
            .build();
  }

  public String emitir(int contaId, String perfil) {
    Instant agora = Instant.now();
    return JWT.create()
        .withIssuer(EMISSOR)
        .withAudience("livraria")
        .withSubject(Integer.toString(contaId))
        .withClaim("perfil", perfil)
        .withIssuedAt(agora)
        .withExpiresAt(agora.plusSeconds(1800))
        .sign(algoritmo);
  }

  public DecodedJWT verificar(String token) {
    return verificador.verify(token);
  }
}
