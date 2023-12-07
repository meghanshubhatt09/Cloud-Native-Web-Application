package com.webservices.cloudwebapp.webapp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.stereotype.Service;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;



@Entity// java persistence for making table in DB
@Service
@Table(name = "UserDB") // we can change the name of the table in DB
public class User implements Serializable {

    @Id // Important
    @GeneratedValue()
    @Column(name = "id")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID id; // Always keep a class and not primitive data-types

    @Column(name = "first_name") // change the name of column if u need
    @NotBlank(message = "firstName is mandatory")
    //@Size(min=2, max=15)
    private String first_name;

    @Column(name = "last_name") // change the name of column if u need
    @NotBlank(message = "lastName is mandatory")
    //@Size(min=2, max=15)
    private String last_name;


    @NotBlank(message = "password is mandatory")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Column(name = "username", unique=true)
    @NotBlank(message = "username is mandatory")
    //@Size(min=2, max=15)
    private String username;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "accountCreated")
    private LocalDateTime account_created;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "accountUpdated")
    private LocalDateTime account_updated;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonIgnore
    private boolean isVerified;
    @JsonIgnore
    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDateTime getAccount_created() {
        return account_created;
    }

    public void setAccount_created(LocalDateTime account_created) {
        this.account_created = account_created;
    }

    public LocalDateTime getAccount_updated() {
        return account_updated;
    }

    public void setAccount_updated(LocalDateTime account_updated) {
        this.account_updated = account_updated;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", firstName='" + first_name + '\'' +
                ", lastName='" + last_name + '\'' +
                ", password='" + password + '\'' +
                ", username='" + username + '\'' +
                ", account_created='" + account_created + '\'' +
                ", account_updated='" + account_updated + '\'' +
                '}';
    }
}
