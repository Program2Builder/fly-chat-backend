package com.chat.fly.app.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.chat.fly.app.auth.model.AppUser;
import com.chat.fly.app.auth.model.UserKeyStore;

@Repository
public interface UserKeyRepository extends JpaRepository<UserKeyStore, Long> {

    Optional<UserKeyStore> findByUser(AppUser user);

    Optional<UserKeyStore> findByUserUsername(String username);
}
