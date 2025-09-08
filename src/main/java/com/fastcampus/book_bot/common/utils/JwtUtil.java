package com.fastcampus.book_bot.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Component
@Data
@ConfigurationProperties(prefix = "jwt")
public class JwtUtil {

    private String secretKey;
    private Long expirationTime;
    private Long refreshExpirationTime;
    private String issuer;

    /** JWT 서명용 키 생성
     * Base64로 인코딩된 비밀키를 디코딩하여 HMAC SHA256 키로 변환
     * */
    private Key getSignatureKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /** Access Token 생성 메서드
     * 사용자명과 권한 정보를 포함한 JWT 토큰 생성
     * @param roles 권한 명
     * */
    public String createAccessToken(Integer userId, String roles) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim("roles", roles)
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(getSignatureKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /** Refresh Token 생성 메서드
     * 사용자 명만 포함하고 권한정보는 제외
     * */
    public String createRefreshToken(Integer userId) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + refreshExpirationTime);

        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(getSignatureKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /** JWT에서 특정 클레임 추출하는 메서드
     * @param claimsResolver Claims에서 원하는 정보를 추출
     * */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSignatureKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claimsResolver.apply(claims);
    }

    /** JWT에서 사용자 ID 추출
     * @param token JWT 토큰 문자열
     * */
    public Integer extractUserId(String token) {
        String userId = extractClaim(token, Claims::getSubject);

        return Integer.parseInt(userId);
    }

    /** JWT에서 권한 추출
     * */
    public String extractRoles(String token) {

        return extractClaim(token, claims -> claims.get("roles", String.class));
    }

    /** JWT 만료여부 확인
     * */
    public boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    /** JWT 유효성 검증
     * */
    public boolean validateToken(String token, Integer userId) {
        final Integer tokenUserId = extractUserId(token);
        return (tokenUserId.equals(userId) && !isTokenExpired(token));
    }

}
