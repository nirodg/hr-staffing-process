package org.db.hrsp.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaPersistEventProducer {

    @Autowired
    private KafkaTemplate<String, KafkaPayload> kafkaTemplate;

    public void publishEvent(KafkaPayload payload) {
        kafkaTemplate.send(payload.getTopic().name(), payload);
    }


}
