package ru.netology.diplomcloudstore.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.security.Keys;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.netology.diplomcloudstore.domain.ProgramSettings;
import ru.netology.diplomcloudstore.entities.UserEntity;
import ru.netology.diplomcloudstore.entities.UserJwtTokenEntity;
import ru.netology.diplomcloudstore.repositories.UserJwtRepository;

import java.security.Key;
import java.util.*;
import java.util.function.Function;

@Service
@Slf4j
@RequiredArgsConstructor
public class JwtService {
    private final UserJwtRepository userJwtRepository;
    private final ProgramSettings programSettings;

    public String extractUsername(String token) {
        return extractClaim(token, claims -> claims.getSubject());
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserEntity user) {
        log.debug("JwtService.saveUserJwtToken.Stage1: Start generate Jwt token...");

        String jwtToken = generateToken(new HashMap<>(), user);
        log.debug(String.format("JwtService.saveUserJwtToken.Stage2: The Jwt token is generated as '%s'", jwtToken));

        revokeAllUserJwtTokens(user.getUsername());
        log.debug(String.format("JwtService.saveUserJwtToken.Stage3: All old Jwt tokens was marked is revoked and expired for user '%s'", user.getUsername()));

        saveUserJwtToken(jwtToken, user);
        log.debug(String.format("JwtService.saveUserJwtToken.Stage4: The Jwt token was saved to database jwt-table for user '%s'", user.getUsername()));
        return jwtToken;
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, programSettings.getExpiration());
    }

    @Transactional
    public void saveUserJwtToken(String jwtToken, UserEntity user) {
        UserJwtTokenEntity token = UserJwtTokenEntity.builder()
                .jwt(jwtToken)
                .createDate(new Date())
                .expired(false)
                .revoke(false)
                .user(user)
                .build();

        userJwtRepository.save(token);
        log.debug(String.format("JwtService.saveUserJwtToken: The Jwt token '%s' is generated successful for user:%s", token, user.getUsername()));
    }


    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, Long expiration) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    @Transactional
    public void revokeAllUserJwtTokens(String username) {
        List<UserJwtTokenEntity> validUserTokens = userJwtRepository.findAllValidUserJwtTokenByUser(username);
        if (validUserTokens.isEmpty()) {
            log.debug(String.format("JwtService.revokeAllUserJwtTokens: Valid Jwt tokens for User:'%s' not found in database", username));
            return;
        }

        validUserTokens.forEach(t -> {
            t.setRevoke(true);
            t.setExpired(true);
        });

        userJwtRepository.saveAll(validUserTokens);
        log.debug(String.format("JwtService.revokeAllUserJwtTokens: All %d Jwt tokens for user  was revoked", validUserTokens.size()));
    }


    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);

        return (username.equals(userDetails.getUsername()))
                && !isTokenExpired(token);
    }


    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, claims -> claims.getExpiration());
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() throws DecodingException {
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(programSettings.getSecret());
        } catch (DecodingException ex) {
            String msg = "JwtService.getSignInKey: Decode the secret key create decoding exception. Check the secret key in database settings table";
            log.error(msg);
            throw new DecodingException(String.join(" ", msg, ex.getMessage()));
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
