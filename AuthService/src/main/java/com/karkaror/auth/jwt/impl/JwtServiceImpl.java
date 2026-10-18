package com.karkaror.auth.jwt.impl;

import com.karkaror.auth.exception.JwtGenerationException;
import com.karkaror.auth.jwt.JwtService;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

/**
 * Реализация {@link JwtService} на Nimbus JOSE+JWT с алгоритмом HS256.
 * <p>HS256 — симметричный алгоритм: один секрет используется и для подписи,
 *    и для проверки. Секрет хранится ТОЛЬКО в AuthService.</p>
 */
@Service
@Slf4j
public class JwtServiceImpl implements JwtService {

    private final byte[] secret;
    private final long accessTtlMinutes;

    public JwtServiceImpl(
            @Value("${jwt.secret}") String secretBase64,
            @Value("${jwt.access-ttl-minutes}") long accessTtlMinutes) {
        // Декодируем Base64-секрет в байты
        this.secret = Base64.getDecoder().decode(secretBase64);
        this.accessTtlMinutes = accessTtlMinutes;
    }

    @Override
    public String generateToken(String userId, String email) {
        try {
            Instant now = Instant.now();

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(userId)                            // sub = userId
                    .claim("email", email)                      // кастомный claim
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(now.plus(accessTtlMinutes, ChronoUnit.MINUTES)))
                    .build();

            SignedJWT jwt = new SignedJWT(
                    new JWSHeader(JWSAlgorithm.HS256),
                    claims
            );
            jwt.sign(new MACSigner(secret));
            return jwt.serialize();

        } catch (JOSEException e) {
            throw new JwtGenerationException("Failed to generate JWT", e);
        }
    }

    @Override
    public boolean validate(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);

            // 1. Проверяем подпись
            if (!jwt.verify(new MACVerifier(secret))) {
                log.debug("JWT signature verification failed");
                return false;
            }

            // 2. Проверяем срок действия
            Date expiration = jwt.getJWTClaimsSet().getExpirationTime();
            if (expiration == null || expiration.before(new Date())) {
                log.debug("JWT expired");
                return false;
            }

            return true;

        } catch (Exception e) {
            log.debug("JWT validation error: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public Optional<String> extractUserId(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            return Optional.ofNullable(jwt.getJWTClaimsSet().getSubject());
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
