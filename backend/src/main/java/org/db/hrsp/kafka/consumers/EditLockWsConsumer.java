package org.db.hrsp.kafka.consumers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EditLockWsConsumer {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = {"EDIT-LOCKS"}, groupId = "backend-consumer")
    public void onKafkaMessage(String message) {
        log.info("Received Kafka message: " + message);
        messagingTemplate.convertAndSend("/backend-updates", message);
    }
}
