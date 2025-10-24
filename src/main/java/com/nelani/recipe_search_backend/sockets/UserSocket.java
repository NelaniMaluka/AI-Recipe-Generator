package com.nelani.recipe_search_backend.sockets;

import com.nelani.recipe_search_backend.response.UserResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserSocket {

    private final SimpMessagingTemplate messagingTemplate;

    public UserSocket(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendUpdatedUser(UserResponse userResponse) {
        messagingTemplate.convertAndSend("/topic/recipes/" + userResponse.getPublicId(), userResponse);
    }
}
