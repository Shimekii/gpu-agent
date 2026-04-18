package org.example.domain;

import java.time.LocalDateTime;

public class User {
    private String login;
    private String password; // пока что храним строкой (в прототипе)
    private String fullName;
    private UserRole role;
    private LocalDateTime createdAt;

    public User() {}

    public User(String login, String password, String fullName, UserRole role) {
        this.login = login;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
        this.createdAt = LocalDateTime.now();
    }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
