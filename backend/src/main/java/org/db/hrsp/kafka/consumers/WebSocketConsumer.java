package org.db.hrsp.kafka.consumers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WebSocketConsumer {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = {"CLIENTS", "STAFFING_PROCESS", "EMPLOYEES", "COMMENTS"})
    public void onKafkaMessage(String message) {
        log.info("Received Kafka message: " + message);
        //messagingTemplate.convertAndSend("/backend-updates", message);
    }
}
