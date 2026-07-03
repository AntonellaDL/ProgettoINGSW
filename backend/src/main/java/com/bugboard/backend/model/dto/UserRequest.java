package com.bugboard.backend.model.dto;

import com.bugboard.backend.model.Enum.Role;

public class UserRequest {

    private String email;
    private String password; // Qui la password c'è, perché ci serve in fase di registrazione!
    private String name;
    private Role role;

    public UserRequest() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
