package org.db.hrsp.kafka.producers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.db.hrsp.kafka.model.KafkaPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EditLockProducer {

    @Autowired
    private final KafkaTemplate<Object, KafkaPayload> kafkaTemplate;

    public void publishLock(KafkaPayload payload) {
        publishLock(payload, payload.getEntity(), payload.getEntityId(), payload.getUserId(), payload.getAction());
    }

    public void publishLock(KafkaPayload payload, String entity, Long id, String editor, KafkaPayload.Action action) {
        kafkaTemplate.send(payload.getTopic().getTopicName(), payload);
    }
}