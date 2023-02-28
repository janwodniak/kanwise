package com.kanwise.user_service.utils;

import com.auth0.jwt.JWT;
import com.kanwise.user_service.configuration.security.jwt.JwtConfigurationProperties;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;
import static com.kanwise.user_service.constant.SecurityConstant.AUTHORITIES;

@Getter
@Setter
public class JwtTestToken {
    private String issuer;
    private String audience;
    private String username;
    private String[] authorities;
    private Date issuedAt;
    private Date expiration;
    private String secretKey;
    private String tokenPrefix;

    public JwtTestToken(String username, String[] authorities, JwtConfigurationProperties jwtConfigurationProperties) {
        this.issuer = jwtConfigurationProperties.issuer();
        this.audience = jwtConfigurationProperties.audience();
        this.username = username;
        this.authorities = authorities;
        this.issuedAt = new Date();
        this.expiration = new Date(System.currentTimeMillis() + jwtConfigurationProperties.expirationAfter().toMillis());
        this.secretKey = jwtConfigurationProperties.secretKey();
        this.tokenPrefix = jwtConfigurationProperties.tokenPrefix();
    }

    public String generateTokenAsString() {
        return JWT.create()
                .withIssuer(issuer)
                .withAudience(audience)
                .withIssuedAt(issuedAt)
                .withSubject(username)
                .withArrayClaim(AUTHORITIES, authorities)
                .withExpiresAt(expiration)
                .sign(HMAC512(secretKey.getBytes()));
    }
}
