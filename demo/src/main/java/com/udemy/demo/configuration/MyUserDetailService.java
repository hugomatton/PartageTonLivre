package com.udemy.demo.configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.udemy.demo.user.User;
import com.udemy.demo.user.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

//Permet d'aller voir en base si l'utilisateur existe
@Service
public class MyUserDetailService implements UserDetailsService{

    @Autowired
    UserRepository userRepository; 

    
    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        List<User> users = userRepository.findByEmail(login);
        //On verifie qu'il y a bien un user possèdant ce login
        if(users.isEmpty()){
            throw new UsernameNotFoundException(login);
        }
        return new UserPrincipal(users.get(0)); //On retourne utilisateur Principal (actif)
    }

    //Classe de l'user Principal
    //UserDeatils contient tout les détails de l'utilisateur

    public static class UserPrincipal implements UserDetails{
        
        private User user;

        public UserPrincipal(User user){ //User en param --> user retourné parloadUserByUsername
            this.user = user;
        }

        public void setUser(User user){
            this.user = user;
        }

        public User getUser(){
            return this.user;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            //On utilise pas mais c'est necessaire pour sring security
            //C'est un ensemble de droit que l'utilisateur a
            //Dans otre appli tout le monde a les mêmes droits
            final List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));//Role user normal par default
            return authorities;
        }

        @Override
        public String getPassword() {
            return this.user.getPassword();
        }

        @Override
        public String getUsername() {
            return this.user.getEmail();
        }

        @Override
        public boolean isAccountNonExpired() {
            // TODO Auto-generated method stub
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }

}