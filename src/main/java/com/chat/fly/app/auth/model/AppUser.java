package com.chat.fly.app.auth.model;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "users")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String username;

    @Column(nullable = false, unique = true, length = 128)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 128)
    private String displayName;

    @Column(nullable = false)
    private boolean enabled = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role", nullable = false, length = 64)
    private Set<String> roles = new HashSet<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Column(length = 64)
    private String profilePictureId;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_contacts",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "contact_id"))
    private Set<AppUser> contacts = new HashSet<>();

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    // public Long getId() {
    // return id;
    // }

    // public void setId(Long id) {
    // this.id = id;
    // }

    // public String getUsername() {
    // return username;
    // }

    // public void setUsername(String username) {
    // this.username = username;
    // }

    // public String getEmail() {
    // return email;
    // }

    // public void setEmail(String email) {
    // this.email = email;
    // }

    // public String getPasswordHash() {
    // return passwordHash;
    // }

    // public void setPasswordHash(String passwordHash) {
    // this.passwordHash = passwordHash;
    // }

    // public String getDisplayName() {
    // return displayName;
    // }

    // public void setDisplayName(String displayName) {
    // this.displayName = displayName;
    // }

    // public boolean isEnabled() {
    // return enabled;
    // }

    // public void setEnabled(boolean enabled) {
    // this.enabled = enabled;
    // }

    // public Set<String> getRoles() {
    // return roles;
    // }

    // public void setRoles(Set<String> roles) {
    // this.roles = roles;
    // }

    // public Instant getCreatedAt() {
    // return createdAt;
    // }

    // public Instant getUpdatedAt() {
    // return updatedAt;
    // }
}
