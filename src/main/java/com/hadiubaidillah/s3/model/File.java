package com.hadiubaidillah.s3.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Entity
public class File {


    @JsonIgnore
    private HibernateProxy hibernateLazyInitializer;


    @Id
    //@GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String name;

    //private String extension;

    @Column(name = "mime_type")
    private String mimeType;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_by")
    private String deletedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public File() {

    }

    public File(UUID id) {
        this.id = id;
    }

    // Getters and setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

//    public String getExtension() {
//        return extension;
//    }
//
//    public void setExtension(String extension) {
//        this.extension = extension;
//    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    // Get extension by mimeType, else by file name extension
    public String getExtension() {
        return Optional.ofNullable(mimeType)
                .map(mt -> mt.substring(mt.lastIndexOf("/") + 1))
                .orElseGet(() -> Optional.ofNullable(name)
                        .filter(name -> name.contains("."))
                        .map(name -> name.substring(name.lastIndexOf(".") + 1))
                        .orElse(null));
    }

}