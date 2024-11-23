package com.taskmanager.myapp.config.jwt;

import com.taskmanager.myapp.exception.CustomInternalException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtTokenUtil {

    private static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
    private static final String REFRESH_TOKEN_SUBJECT = "RefreshToken";
    private static final String TOKEN_CLAIM = "employeeNumber";

    private final SecretKey secretKey;
    private final Long accessTokenExpiration;
    private final Long refreshTokenExpiration;

    public JwtTokenUtil(@Value("${jwt.secretKey}") String secretKey,
                        @Value("${jwt.access.expiration}") Long accessTokenExpiration,
                        @Value("${jwt.refresh.expiration}") Long refreshTokenExpiration) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(secretKey));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public Long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    public String generateAccessToken(String employeeNumber) {
        log.info(">>> Generate Access Token <<<");

        return createToken(ACCESS_TOKEN_SUBJECT, employeeNumber, accessTokenExpiration);
    }

    public String generateRefreshToken(String employeeNumber) {
        log.info(">>> Generate Refresh Token <<<");

        return createToken(REFRESH_TOKEN_SUBJECT, employeeNumber, refreshTokenExpiration);
    }

    public String extractEmployeeNumber(String token) {
        log.info(">>> Extract Employee Number <<<");

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return (String) claims.get(TOKEN_CLAIM);
    }

    public Boolean validateToken(String token, String employeeNumber) {
        log.info(">>> Check validate Token <<<");

        if (isTokenExpired(token)) {
            log.info(">>> Expired Token <<<");

            return false;
        }

        final String extractEmployeeNumber = extractEmployeeNumber(token);

        return extractEmployeeNumber.equals(employeeNumber);
    }

    public Date getExpiration(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getExpiration();
        } catch (ExpiredJwtException e) {
            log.info(">>> Expired Jwt Token Exception <<<");

            return null;
        } catch (Exception e) {
            log.info(">>> Invalid Token <<<");

            return null;
        }
    }

    public String tokenToHash(String accessToken) {
        log.info(">>> Token To Hash <<<");

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hashBytes = digest.digest(accessToken.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hashBytes) {
                hexString.append(String.format("%02x", b));
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new CustomInternalException("Generate Access Token Error");
        }
    }

    private boolean isTokenExpired(String token) {
        try {
            Date expiration = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();

            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (Exception e) {
            return true;
        }

    }

    private String createToken(String subject, String claim, Long expiration) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(TOKEN_CLAIM, claim);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + expiration))
                .signWith(secretKey)
                .compact();
    }

}