package com.chat.fly.app.auth.model;

import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_key_stores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserKeyStore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private AppUser user;

    @Column(nullable = false)
    private int registrationId;

    @Column(nullable = false, length = 128)
    private String identityPublicKey;

    @Column(nullable = false)
    private int signedPreKeyId;

    @Column(nullable = false, length = 128)
    private String signedPreKey;

    @Column(nullable = false, length = 128)
    private String signedPreKeySignature;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_one_time_pre_keys", joinColumns = @JoinColumn(name = "key_store_id"))
    @jakarta.persistence.MapKeyColumn(name = "key_id")
    @Column(name = "pre_key", nullable = false, length = 128)
    private Map<Integer, String> oneTimePreKeys = new HashMap<>();
}
