package com.SpendWise.ExpenseTracker.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    private final Key SECRET_KEY = Keys.hmacShaKeyFor(
            "mySecretKeymySecretKeymySecretKey123456".getBytes()
    );
    private final long EXPIRATION_TIME = 1000 * 60 * 60 * 10;

    public String generateToken(String email, String username){
        return Jwts.builder()// builder is a pattern used to create an object step by step, instead of one big constructor
                .setSubject(email)
                /*
                This puts the email into the token’s sub claim
                subject means: the main identity this token is about.
                */
                .setIssuedAt(new Date())
                .claim("username", username)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact(); //finishes the token building process and converts it into the final JWT string
    }

    public String extractEmail(String token){
        return extractClaims(token).getSubject();
        /*
        This means:
                  •first read the token and extract its claims
                  •then get the subject field from those claims
        */
    }

    public boolean validateToken(String token, String email){
        String extractedEmail = extractEmail(token);
        return extractedEmail.equals(email) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token){
        return extractClaims(token).getExpiration().before(new Date());
    }

    private Claims extractClaims(String token){
        return Jwts.parser()// starts the process of reading/parsing JWT
                .setSigningKey(SECRET_KEY)//tells the parser which secret key was used to sign the token, needed to verify token signature
                .parseClaimsJws(token)//parses the Jwt string
                .getBody();//gets the payload part of the token, that payload is represented as Claims
    }
}

