package com.example.livestream_apd.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


@Component
@Slf4j
public class JwtTokenProvider {
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${app.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Value("${app.jwt.issuer}")
    private String issuer;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    public String generateAccessToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        return generateAccessToken(userPrincipal.getUsername());
    }

    public String generateAccessToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "access");
        return createToken(claims,username, accessTokenExpiration);
    }

    public String generateRefreshToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return createToken(claims,username, refreshTokenExpiration);
    }

    private String createToken(Map<String, Object> claims, String subject, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        return Jwts.builder()
                    .claims(claims)
                    .subject(subject)
                    .issuer(issuer)
                    .issuedAt(now)
                    .expiration(expiryDate)
                    .signWith(getSigningKey())
                    .compact();
    }

    public String getUsernameFromToken(String token) {
        return getClaimFromToken (token, Claims::getSubject);
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken (token, Claims::getExpiration);
    }

    public String getTokenType(String token) {
        return getClaimFromToken (token, claims -> claims.get("type",String.class));
    }

    public String getUserIdFromToken(String token) {
        return getClaimFromToken (token, claims -> claims.get("userId",String.class));
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        try{
            return Jwts.parser()
                        .verifyWith(getSigningKey())
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();
        }catch (JwtException e){
            throw e;
        }
    }

    public boolean isTokenExpired(String token) {
        try{
            final Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        }catch (Exception e){
            return true;
        }
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = getUsernameFromToken(token);
            final String tokenType = getTokenType(token);

            // If userDetails is provided, verify username matches
            if (userDetails != null) {
                if (username == null || !username.equals(userDetails.getUsername())) {
                    log.debug("Token username doesn't match UserDetails username");
                    return false;
                }
            }

            if (isTokenExpired(token)) {
                log.debug("Token is expired");
                return false;
            }

            if (!"access".equals(tokenType)) {
                log.debug("Invalid token type: {}", tokenType);
                return false;
            }

            return true;
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public boolean validateRefreshToken(String token) {
        try {
            final String tokenType = getTokenType(token);
            return !isTokenExpired(token)
                    && "refresh".equals(tokenType);
        } catch (Exception e) {
            return false;
        }
    }

    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }
}
