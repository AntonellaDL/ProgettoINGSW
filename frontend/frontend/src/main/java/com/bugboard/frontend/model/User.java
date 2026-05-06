package com.bugboard.frontend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties (ignoreUnknown= true)
public class User {
    private Long id;
    private String email;
    private String name;
    private String role;

    public User(){}

    public User(Long id, String email, String name, String role){
        this.id = id;
        this.email = email;
        this.name = name;
        this.role = role;
    }

    public Long getId(){
        return id;
    }

    public void setId(Long id){
        this.id = id;
    }

    public String getEmail (){
        return email;
    }

    public void setEmail (String email){
        this.email = email;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }
    
    public String getRole (){
        return role;
    }

    public void setRole(String role){
        this.role = role;
    }

    public boolean isAdmin(){
        return "ADMIN".equalsIgnoreCase(this.role);
    }

    @Override
    public String toString(){
        return name +"("+email+")";
    }

    @Override
    public boolean equals(Object o){
        if(this==o) return true;
        if((o==null)||getClass()!=o.getClass()) return false;
        User user= (User) o;
        return id!=null && id.equals(user.id);
    }

    @Override
    public int hashCode(){
        return getClass().hashCode();
    }

}
