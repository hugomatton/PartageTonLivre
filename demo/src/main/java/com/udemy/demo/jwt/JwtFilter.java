package com.udemy.demo.jwt;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.udemy.demo.configuration.MyUserDetailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    MyUserDetailService service;

    @Autowired
    Jwtutils jwtutils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        //Début méthode
        String requestURI = request.getRequestURI();//on récupère url de la requête
        Cookie[] cookies = request.getCookies();//on récupère cookie qui contient token

        //on verifie que URL ne fait pas partie des URL authorisé
        if(getAuthorizedUrls(requestURI) || cookies == null){
            filterChain.doFilter(request, response);
            return;
        }

        String jwtToken = getJwtTokenFromCookie(cookies);
        if(jwtToken == null){
            filterChain.doFilter(request, response);
            return;
        }

        //si token ok (valide et pas expiré)
        String username;
        try {
            username = jwtutils.getUsernameFromToken(jwtToken);
        } catch (Exception e) {
            e.printStackTrace();
            filterChain.doFilter(request, response);
            return;
        }

        //si username existe et que l'user n'est pas connecté
        if(username != null && SecurityContextHolder.getContext().getAuthentication() == null){
            UserDetails userDetails;
            try {
                userDetails  = service.loadUserByUsername(username);
            } catch (Exception e) {
                e.printStackTrace();
                filterChain.doFilter(request, response);
                return;
            }
            //Si le token à le même nom que celui récupéré dans la bdd on est bon
            if(jwtutils.validateToken(jwtToken, userDetails)){
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken
                = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken.setDetails(userDetails);
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        filterChain.doFilter(request, response);
        
    }

    private boolean getAuthorizedUrls(String requestURI){
        return requestURI.equals("/users");
    }

    private String getJwtTokenFromCookie(Cookie[] cookies){
        for(Cookie cookie : cookies){
            if(cookie.getName().equals("token")){
                return cookie.getValue();
            }
        }
        return null;
    }
    
}
