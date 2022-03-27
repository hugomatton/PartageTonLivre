package com.udemy.demo.jwt;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.udemy.demo.configuration.MyUserDetailService.UserPrincipal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

//Classe qui génère le token
@Component
public class Jwtutils {

    public static final long JWT_TOKEN_VALIDITY = 5*60*60;

    @Value("${jwt.secret}")
    private String secret;

    //génration token à partir du principal
    public String generateToken(UserPrincipal principal) {
        Map<String, Object> claims = new HashMap<>();//Pas de claims dans ce token
        return Jwts.builder().setClaims(claims)
                            .setSubject(principal.getUsername())
                            .setIssuedAt(new Date(System.currentTimeMillis()))
                            .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY*1000))
                            .signWith(SignatureAlgorithm.HS512, secret).compact();//secret permet de crypter le token
    }

    /**
     * Permet de valider le token
     * Que le token dans le cookie est correct et qu'il correspond à un utilisateur
     * @param token
     * @param userDetails
     * @return
     */
    public boolean validateToken(String token, UserDetails userDetails){
        final String username = getUsernameFromToken(token);
        //on regarde si
        //--> nom dans token = nom du principal
        //--> on regarde si le token n'est pas expiré
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token){
        final Date expiration = getExpirationDateFrom(token);
        return expiration.before(new Date());
    }

    public Date getExpirationDateFrom( String token){
        return getClaimFromToken(token, Claims::getExpiration);
    }

    String getUsernameFromToken(String token) {
        return getClaimFromToken (token, Claims::getSubject);
    }
    private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getALLCLaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    //for retrieveing any information from token we will. need the secret key
    private Claims getALLCLaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }
}
