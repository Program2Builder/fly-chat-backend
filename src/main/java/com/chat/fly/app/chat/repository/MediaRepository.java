package com.chat.fly.app.chat.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.chat.fly.app.chat.model.Media;

public interface MediaRepository extends MongoRepository<Media, String> {

}
