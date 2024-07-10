package org.aut.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtHandler {

    private static final String SECRET_PASS = // Safer in an encrypted file.
            "@AmirKabirUniversity-Atharifard-Akbari-2024";
    private static final SecretKeySpec SECRET_KEY = // same key every runtime.
            new SecretKeySpec(SECRET_PASS.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

    public static String generateToken(String subject) {
        if (subject == null || subject.isEmpty()) return null;

        long currentTimeMillis = System.currentTimeMillis();
        return Jwts.builder()
                .subject(subject) // user id
                .issuedAt(new Date(currentTimeMillis)) // generation time
                .expiration(new Date(currentTimeMillis + 600000000)) // expiration time
                .signWith(SECRET_KEY).compact();
    }

    public static Claims verifyToken(String token) {
        if (token == null || token.isEmpty()) return null;

        try {
            return Jwts.parser().verifyWith(SECRET_KEY).build().parseSignedClaims(token).getPayload();
        } catch (JwtException e ) {
            return null;
        }
    }
}