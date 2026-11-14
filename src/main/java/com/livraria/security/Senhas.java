package com.livraria.security;

import java.security.*;
import java.util.*;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** PBKDF2 com salt individual. O banco nunca recebe a senha em texto. */
public final class Senhas {
  private static final int ITERACOES = 600_000;

  private Senhas() {}

  public static String gerarHash(String senha) {
    if (senha == null || senha.length() < 12 || senha.length() > 200) {
      throw new IllegalArgumentException("A senha deve ter entre 12 e 200 caracteres.");
    }
    byte[] salt = new byte[16];
    new SecureRandom().nextBytes(salt);
    var base64 = Base64.getEncoder();
    return ITERACOES
        + ":"
        + base64.encodeToString(salt)
        + ":"
        + base64.encodeToString(derivar(senha, salt, ITERACOES));
  }

  public static boolean verificar(String senha, String hash) {
    if (senha == null || senha.length() > 200 || hash == null) return false;
    try {
      String[] partes = hash.split(":");
      int iteracoes = Integer.parseInt(partes[0]);
      if (iteracoes < ITERACOES || iteracoes > 2_000_000 || partes.length != 3) return false;
      byte[] salt = Base64.getDecoder().decode(partes[1]);
      byte[] esperado = Base64.getDecoder().decode(partes[2]);
      return MessageDigest.isEqual(esperado, derivar(senha, salt, iteracoes));
    } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
      return false;
    }
  }

  private static byte[] derivar(String senha, byte[] salt, int iteracoes) {
    var spec = new PBEKeySpec(senha.toCharArray(), salt, iteracoes, 256);
    try {
      return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
    } catch (GeneralSecurityException e) {
      throw new IllegalStateException("PBKDF2 indisponível", e);
    } finally {
      spec.clearPassword();
    }
  }
}
