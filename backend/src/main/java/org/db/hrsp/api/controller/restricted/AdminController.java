package org.db.hrsp.api.controller.restricted;

import lombok.RequiredArgsConstructor;
import org.db.hrsp.kafka.model.KafkaPayload;
import org.db.hrsp.kafka.producers.PersistEventProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    @Autowired
    private PersistEventProducer eventProducer;

    @PostMapping("/kafka")
    public void genKafkaMsg(){
        eventProducer.publishEvent(KafkaPayload.builder().action(KafkaPayload.Action.CREATE).topic(KafkaPayload.Topic.CLIENTS).build());
    }
}
