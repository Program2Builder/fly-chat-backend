package com.chat.fly.app.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.chat.fly.app.auth.repository.AppUserRepository;
import com.chat.fly.app.chat.repository.ChatGroupRepository;
import com.chat.fly.app.chat.repository.ChatMessageRepository;
import com.chat.fly.app.chat.repository.MediaRepository;

@Configuration
@EntityScan(basePackages = { "com.chat.fly.app.auth.model", "com.chat.fly.app.chat.model" })
@EnableJpaRepositories(basePackageClasses = { AppUserRepository.class, ChatGroupRepository.class })
@EnableMongoRepositories(basePackageClasses = { ChatMessageRepository.class, MediaRepository.class })
public class PersistenceConfig {
}
