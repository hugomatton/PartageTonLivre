package com.udemy.demo.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

import javax.servlet.http.Cookie;

import com.udemy.demo.jwt.JwtFilter;

@Configuration //classe de configuration qui va déclarer des beans spring
@EnableWebSecurity //Anotation spring-security
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    
    @Autowired
    private MyUserDetailService service; //Pour avoir accés au Principal (user connecté)

    @Autowired
    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @Autowired
    private JwtFilter jwtFilter;

    //Racine de la configuration de notre sécurité
    @Override
    protected void configure(HttpSecurity http) throws Exception{
        http
            .csrf().disable() //csrf --> faille de sécurité répandu (pas necessaire dans notre cas mais en rpod faut se renseigner)
            .exceptionHandling() //pour que sring-security gère erreur
            .authenticationEntryPoint(restAuthenticationEntryPoint)//Gestion du comportement par defaut de spring-s qui renvoie une page de login (qu'on ne désire pas)
            .and()//and --> permet d'ajouter focntionnalité
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)//pour que les données ne soit pas stoké en session (avantage du jwt)
            .and()
            .authorizeRequests()//On précise quelles url sont authorisées ou non (par défault tout est interdit)
            .antMatchers("/users").permitAll() // Autorisé pour créer utilisateur (POST)
            .antMatchers("/authenticate").permitAll() //tout le monde a le droit de se connecter
            .antMatchers("/isConnected").permitAll()
            .anyRequest().authenticated() //Précise que toutes les autres requêtes doivent être authentifiées
            .and()
            .formLogin()//gére l'aspect login mot de passe
            .and()
            .logout()//gère le logout --> destruction token
            .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK))//Quand logout est un succès
            .addLogoutHandler((request, response, auth)->{ //On applique cette instruction pour supprimer Cookie
                for(Cookie cookie : request.getCookies()){
                    String cookieName = cookie.getName();
                    Cookie cookieToDelete = new Cookie(cookieName, null);//null permet de supprimer le cookie
                    cookieToDelete.setMaxAge(0);
                    response.addCookie(cookieToDelete);//on retourne le cookie dans la reponse http --> ça le clean
                }
            });

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    public void configure(WebSecurity web) throws Exception{
        web.ignoring().antMatchers(
            "/v3/api-docs/**",
            "/swagger-ressources/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/webjars/**"
        );
    }

    //permet de relier configuration security à user detail service qui retourne le Principal
    @Autowired//Spring execute méthode automatiquement
    public void ConfigureGlobal(AuthenticationManagerBuilder auth) throws Exception{
        auth.userDetailsService(service).passwordEncoder(passwordEncoder());
        //set userDetailService
        //set le cryptage de mot de passe
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder(); //Méthode de cryptage
    }

    @Bean
    @Override
    //Pour activer authentification manager --> gestion authentification
    //importent pour que spring security gère aspect authentification
    public AuthenticationManager authenticationManagerBean() throws Exception{
        return super.authenticationManagerBean();
    }
}
