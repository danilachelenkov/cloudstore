package ru.netology.diplomcloudstore.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.netology.diplomcloudstore.entities.UserJwtToken;
import ru.netology.diplomcloudstore.repositories.UserJwtRepository;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {
    @Value("${netology.jwt.secret.key}")
    private String secretKey;

    @Value("${netology.jwt.secret.expiration}")
    private long jwtExpiration;

    private final UserJwtRepository userJwtRepository;

    public String extractUsername(String token) {
        return extractClaim(token, claims -> claims.getSubject());
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        String jwt = generateToken(new HashMap<>(), userDetails);
        userJwtRepository.insertJwt(userDetails.getUsername(), jwt);
        return jwt;
    }

    public String getJwtFromDb(String username) {

        Optional<UserJwtToken> userJwtToken = userJwtRepository.findUserJwtTokenByUserAndActual(username);
        if (userJwtToken.isPresent()) {
            return userJwtToken.get().getJwt();
        }

        return "";
    }

    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    public long getExpirationTime() {
        //todo брать из базы
        return jwtExpiration;
    }

    public String getSecretKey() {
        //todo брать из базы
        return "";
    }


    public void deleteJwtFromDB(String username){
        userJwtRepository.deleteJwt(username);
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);

        return (username.equals(userDetails.getUsername()))
                && !isTokenExpired(token)
                && checkTokenFromDb(username, token);
    }

    private boolean checkTokenFromDb(String username, String token) {
        return getJwtFromDb(username).equals(token);
    }

    private boolean isTokenExpired(String token) {
        //todo проверить как работает протухание
        String s = "";
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, claims -> claims.getExpiration());
    }

    private Claims extractAllClaims(String token) {
        Claims claims = Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims;
    }

    private Key getSignInKey() {
        //todo получать secret из БД по максимальной дате и всегда первое значение в запросе
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
