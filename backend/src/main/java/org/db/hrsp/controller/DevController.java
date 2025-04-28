package org.db.hrsp.controller;

import org.db.hrsp.kafka.KafkaPayload;
import org.db.hrsp.kafka.KafkaPersistEventProducer;
import org.db.hrsp.model.Client;
import org.db.hrsp.model.Comment;
import org.db.hrsp.model.StaffingProcess;
import org.db.hrsp.repository.ClientRepository;
import org.db.hrsp.repository.CommentRepository;
import org.db.hrsp.repository.StaffingProcessRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

@Slf4j
@RestController
@RequestMapping("/api/dev")
@RequiredArgsConstructor
public class DevController {


    private final KafkaPersistEventProducer eventProducer;
    private final ClientRepository clientRepository;
    private final StaffingProcessRepository staffingRepository;
    private final CommentRepository commentRepository;

    @PostMapping("/generateRandomData")
    public ResponseEntity<String> generateRandomData() {
        clientRepository.deleteAll();
        staffingRepository.deleteAll();
        commentRepository.deleteAll();

        List<Client> clients = IntStream.range(1, 11)
                .mapToObj(i -> {
                    Client c = new Client();
                    c.setClientName("Client " + i);
                    return clientRepository.save(c);
                })
                .toList();

        Random random = new Random();
        for (int i = 1; i <= 10; i++) {
            StaffingProcess process = new StaffingProcess();
            process.setTitle("Staffing Process " + i);
            process.setClient(clients.get(random.nextInt(clients.size())));
            process.setActive(random.nextBoolean());

            StaffingProcess saved = staffingRepository.save(process);

            for (int j = 0; j < random.nextInt(4); j++) {
                Comment c = new Comment();
                c.setTitle("Comment Title " + j);
                c.setComment("Random feedback " + j);
                c.setCommentParent(null); // no nesting for now
                c.setStaffingProcess(saved);
                commentRepository.save(c);
            }
        }

        return ResponseEntity.ok("Random data generated!");
    }

    @GetMapping("/ws")
    public void trigerWs(){
        log.info("Triggering WebSocket event");
        System.out.println("Triggering WebSocket event");
        KafkaPayload payload = KafkaPayload.builder()
                .topic(KafkaPayload.Topic.STAFFING_PROCESS)
                .action(KafkaPayload.Action.CREATE)
                .userId("1")
                .build();
        eventProducer.publishEvent(payload);
    }
}
