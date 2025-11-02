package com.nelani.recipe_search_backend.sockets;

import com.nelani.recipe_search_backend.model.User;
import com.nelani.recipe_search_backend.response.UserResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserSocket {

    private final SimpMessagingTemplate messagingTemplate;

    public UserSocket(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /** Sends real-time updates when a user's profile or details are updated. */
    public void sendUpdatedUser(UserResponse userResponse) {
        messagingTemplate.convertAndSend("/topic/users/" + userResponse.publicId() + "/update", userResponse);
    }

    /** Sends live updates for a user's liked recipes. */
    public void sendUpdatedUserLikes(User user, List<String> userLikes) {
        messagingTemplate.convertAndSend("/topic/users/" + user.getPublicId() + "/likes", userLikes);
    }
}
