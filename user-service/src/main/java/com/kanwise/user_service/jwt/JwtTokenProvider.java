package com.kanwise.user_service.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.kanwise.user_service.configuration.security.jwt.JwtConfigurationProperties;
import com.kanwise.user_service.model.user.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;
import static com.kanwise.user_service.constant.SecurityConstant.AUTHORITIES;
import static com.kanwise.user_service.constant.SecurityConstant.TOKEN_CANNOT_BE_VERIFIED;
import static java.util.Arrays.stream;


@RequiredArgsConstructor
@Component
public class JwtTokenProvider {

    private final JwtConfigurationProperties jwtConfigurationProperties;

    public String generateToken(UserPrincipal userPrincipal) {
        return JWT.create()
                .withIssuer(jwtConfigurationProperties.issuer())
                .withAudience(jwtConfigurationProperties.audience())
                .withIssuedAt(new Date())
                .withSubject(userPrincipal.getUsername())
                .withArrayClaim(AUTHORITIES, getClaimsForUser(userPrincipal))
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtConfigurationProperties.expirationAfter().toMillis()))
                .sign(HMAC512(jwtConfigurationProperties.secretKey().getBytes()));
    }

    public List<SimpleGrantedAuthority> getAuthorities(String token) {
        return stream(getClaimsFromToken(token))
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    public Authentication getAuthentication(String username, Collection<? extends GrantedAuthority> authorities, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(username, null, authorities);
        usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return usernamePasswordAuthenticationToken;
    }

    public boolean isTokenValid(String username, String token) {
        return StringUtils.isNotEmpty(username) && !isTokenExpired(token);
    }

    public boolean startsWithPrefix(String token) {
        return token.startsWith(jwtConfigurationProperties.tokenPrefix());
    }

    public String getTokenWithoutPrefix(String token) {
        return token.replace(jwtConfigurationProperties.tokenPrefix(), "");
    }

    private boolean isTokenExpired(String token) {
        return getVerifier()
                .verify(token)
                .getExpiresAt()
                .before(new Date());
    }

    public String getSubject(String token) {
        try {
            return getVerifier()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException exception) {
            throw new JWTVerificationException(TOKEN_CANNOT_BE_VERIFIED);
        }
    }

    private JWTVerifier getVerifier() {
        JWTVerifier verifier;
        try {
            verifier = JWT.require(HMAC512(jwtConfigurationProperties.secretKey().getBytes()))
                    .withIssuer(jwtConfigurationProperties.issuer())
                    .withAudience(jwtConfigurationProperties.audience())
                    .build();
        } catch (JWTVerificationException exception) {
            throw new JWTVerificationException(TOKEN_CANNOT_BE_VERIFIED);
        }
        return verifier;
    }

    private String[] getClaimsFromToken(String token) {
        return getVerifier()
                .verify(token)
                .getClaim(AUTHORITIES)
                .asArray(String.class);
    }

    private String[] getClaimsForUser(UserPrincipal userPrincipal) {
        return userPrincipal.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toArray(String[]::new);
    }
}
