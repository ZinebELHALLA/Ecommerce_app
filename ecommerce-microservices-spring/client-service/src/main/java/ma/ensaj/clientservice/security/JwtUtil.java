package ma.ensaj.clientservice.security;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;
@Component
public class JwtUtil {
    @Value("${jwt.secret:mySecretKeyForJWTTokenGenerationAndValidation1234567890}")
    private String secret;
    @Value("${jwt.expiration:86400000}") // 24 hours in milliseconds
    private long expiration;
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
    public String generateToken(Long userId, String username, String role) {
        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("username", username)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    public String extractUsername(String token) {
        return extractClaims(token).get("username", String.class);
    }
    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }
    public Long extractUserId(String token) {
        return Long.parseLong(extractClaims(token).getSubject());
    }
    public boolean validateToken(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}