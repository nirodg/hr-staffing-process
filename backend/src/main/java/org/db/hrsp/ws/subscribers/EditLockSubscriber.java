package org.db.hrsp.ws.subscribers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class EditLockSubscriber {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void onMessage(Message message, byte[] pattern) {
        String payload = new String(message.getBody(), StandardCharsets.UTF_8);
        messagingTemplate.convertAndSend("/topic/edit-locks", payload);
    }
}
