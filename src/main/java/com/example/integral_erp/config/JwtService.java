package com.example.integral_erp.config;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.example.integral_erp.usuario.Usuario;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    @Value("${spring.jwt.secret}")
    private String secret;

    private Key getSigningKey() {

        try {
            byte[] keyBytes = secret.getBytes();
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        }
        
    }

    public String gerarToken(Usuario usuario) {

        // 30 dias em milisegundos
        long trintaDiasMs = 1000L * 60 * 60 * 24 * 30;

        return Jwts.builder()
            .setSubject(usuario.getEmail())
            .claim("role", usuario.getRole().name())
            .claim("centroId", 
                usuario.getCentro() != null ? usuario.getCentro().getId() : null)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + trintaDiasMs))
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    public String extrairEmail(String token) {
        return extrairClaims(token).getSubject();
    }

    public Claims extrairClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    public boolean tokenValido(String token, UserDetails userDetails) {
        String email = extrairEmail(token);
        return email.equals(userDetails.getUsername())
            && !extrairClaims(token).getExpiration().before(new Date());
    }
}
