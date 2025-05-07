package org.db.hrsp.kafka.producers;

import org.db.hrsp.kafka.model.KafkaPayload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PersistEventProducer {

    @Autowired
    private KafkaTemplate<String, KafkaPayload> kafkaTemplate;

    public void publishEvent(KafkaPayload payload) {
        kafkaTemplate.send(payload.getTopic().name(), payload);
    }


}
