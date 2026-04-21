package com.ashuu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ashuu.model.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

}
