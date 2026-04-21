package com.ashuu.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ashuu.model.Message;
import com.ashuu.repository.MessageRepository;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "*")
public class MessageController {

	@Autowired
	private SimpMessagingTemplate messagingTemplate;

    private final MessageRepository messageRepository;

    public MessageController(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

	// ===============================
	// USER SEND MESSAGE (CONTACT FORM)
	// ===============================
	@PostMapping("/contact")
	public Message sendMessage(@RequestBody Message message) {

		message.setRead(false);
		Message saved = messageRepository.save(message);

		messagingTemplate.convertAndSend("/topic/messages", saved);

		return saved;
	}

	// ===============================
	// ADMIN GET ALL MESSAGES
	// ===============================
    @GetMapping
    public List<Message> getAllMessages() {
        return messageRepository.findAll();
    }

	// ===============================
	// GET SINGLE MESSAGE
	// ===============================
    @GetMapping("/{id}")
    public Message getMessage(@PathVariable Long id) {

		Message message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));

		return message;
    }

	// ===============================
	// MARK MESSAGE AS READ
	// ===============================
	@PutMapping("/{id}/read")
	public Message markAsRead(@PathVariable Long id) {

        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));

		message.setRead(true);

        return messageRepository.save(message);
    }

	@PutMapping("/{id}/star")
	public Message toggleStar(@PathVariable Long id) {

		Message message = messageRepository.findById(id).orElseThrow();

		message.setStarred(!message.isStarred());

		return messageRepository.save(message);
	}

	// ===============================
	// DELETE MESSAGE
	// ===============================
    @DeleteMapping("/{id}")
    public void deleteMessage(@PathVariable Long id) {
        messageRepository.deleteById(id);
    }

}