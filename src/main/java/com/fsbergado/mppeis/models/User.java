package com.fsbergado.mppeis.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * User
 */
@JsonPropertyOrder({"id", "email", "role", "is_active", "email_verifiet_at"})
public class User extends Model {

    private Integer id;

    private Integer role;

    private String email;

    private String password;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("email_verified_at")
    private String emailVerifiedAt;

    public User() {
        super();
    }

    public Integer getId() {
        return id;        
    }

    public void setId(Integer id) {
        this.id = id;        
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getRole() {
        return role;
    }

    public void setRole(Integer role) {
        this.role = role;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        this.isActive = active;
    }

    public String getEmailVerifiedAt() {
        return emailVerifiedAt;
    }

    public void setEmailVerifiedAt(String timestamp) {
        this.emailVerifiedAt = timestamp;
    }
}