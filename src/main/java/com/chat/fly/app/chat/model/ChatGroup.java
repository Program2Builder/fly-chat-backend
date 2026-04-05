package com.chat.fly.app.chat.model;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

import com.chat.fly.app.auth.model.AppUser;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "chat_groups")
public class ChatGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String roomId;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private boolean autoJoin = false;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private AppUser creator;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "chat_group_members",
            joinColumns = @JoinColumn(name = "chat_group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<AppUser> members = new LinkedHashSet<>();

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isAutoJoin() {
        return autoJoin;
    }

    public void setAutoJoin(boolean autoJoin) {
        this.autoJoin = autoJoin;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public AppUser getCreator() {
        return creator;
    }

    public void setCreator(AppUser creator) {
        this.creator = creator;
    }

    public Set<AppUser> getMembers() {
        return members;
    }

    public void setMembers(Set<AppUser> members) {
        this.members = members;
    }
}
