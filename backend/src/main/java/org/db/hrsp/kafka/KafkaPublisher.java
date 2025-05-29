package org.db.hrsp.kafka;

import lombok.AllArgsConstructor;
import org.db.hrsp.api.common.UpstreamFailureException;
import org.db.hrsp.api.config.security.JwtInterceptor;
import org.db.hrsp.kafka.model.KafkaPayload;
import org.db.hrsp.kafka.producers.PersistEventProducer;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class KafkaPublisher {

    private final PersistEventProducer eventProducer;
    private final JwtInterceptor jwtInterceptor;

    public void publish(KafkaPayload payload) {
        try {
            eventProducer.publishEvent(payload);
        } catch (RuntimeException ex) {
            throw new UpstreamFailureException("Failed to publish Kafka event");
        }
    }

    public void publish(KafkaPayload.Topic topic, KafkaPayload.Action action) {
        publish(KafkaPayload.builder()
                .userId(jwtInterceptor.getCurrentUser().getUsername())
                .action(action)
                .topic(topic)
                .build());
    }
}
