package com.webservices.cloudwebapp.webapp.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity// java persistence for making table in DB
@Service
@Table(name = "DocumentDB") // we can change the name of the table in DB
public class Document {

    @Id // Important
    @GeneratedValue()
    @Column(name = "doc_id")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID doc_id; // Always keep a class and not primitive data-types

    @Column(name = "user_id")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JdbcTypeCode(SqlTypes.CHAR)
    private UUID user_id; // Always keep a class and not primitive data-types

    @Column(name = "name") // change the name of column if u need
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String name;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "date_created")
    private LocalDateTime date_created;

    @Column(name = "s3_bucket_path") // change the name of column if u need
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String s3_bucket_path;

    public UUID getDoc_id() {
        return doc_id;
    }

    public void setDoc_id(UUID doc_id) {
        this.doc_id = doc_id;
    }

    public UUID getUser_id() {
        return user_id;
    }

    public void setUser_id(UUID user_id) {
        this.user_id = user_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getDate_created() {
        return date_created;
    }

    public void setDate_created(LocalDateTime date_created) {
        this.date_created = date_created;
    }

    public String getS3_bucket_path() {
        return s3_bucket_path;
    }

    public void setS3_bucket_path(String s3_bucket_path) {
        this.s3_bucket_path = s3_bucket_path;
    }

    @Override
    public String toString() {
        return "Document{" +
                "doc_id=" + doc_id +
                ", user_id=" + user_id +
                ", name='" + name + '\'' +
                ", date_created=" + date_created +
                ", s3_bucket_path='" + s3_bucket_path + '\'' +
                '}';
    }
}
