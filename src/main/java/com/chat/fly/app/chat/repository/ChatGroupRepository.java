package com.chat.fly.app.chat.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chat.fly.app.chat.model.ChatGroup;

public interface ChatGroupRepository extends JpaRepository<ChatGroup, Long> {

    Optional<ChatGroup> findByRoomId(String roomId);

    List<ChatGroup> findDistinctByMembersUsernameOrderByNameAsc(String username);

    List<ChatGroup> findByAutoJoinTrueOrderByNameAsc();
}
