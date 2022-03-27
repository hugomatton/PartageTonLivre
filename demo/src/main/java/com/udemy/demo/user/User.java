package com.udemy.demo.user;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.Size;

@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    private String email;

    @Size(min=2, max=25, message="le prenom doit faire entre 2 et 25 caractères")
    private String firstName;

    @Size(min=2, max=25, message="le nom doit faire entre 2 et 25 caractères")
    private String lastName;

    private String password;

    public User(String email, String firstname, String lastName){
        this.email = email;
        this.firstName = firstname;
        this.lastName = lastName;
    }

    public User(){
        
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    
    
}
