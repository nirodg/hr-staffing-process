package org.db.hrsp.api;

import lombok.RequiredArgsConstructor;
import org.db.hrsp.api.config.security.JwtInterceptor;
import org.db.hrsp.kafka.model.KafkaPayload;
import org.db.hrsp.kafka.producers.EditLockProducer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@RestController
@RequestMapping("/api/editing")
@RequiredArgsConstructor
public class LiveEditController {

    private final EditLockProducer editLockProducer;
    private final JwtInterceptor jwt;

    // In-memory lock registry (replace Redis)
    private final Map<String, String> activeLocks = new ConcurrentHashMap<>();

    @PostMapping("/{entity}/{id}/start")
    public void startEditing(@PathVariable String entity, @PathVariable Long id) {
        String key = generateKey(entity, id);
        String username = jwt.getCurrentUser().getUsername();
        activeLocks.put(key, username);


        KafkaPayload payload = KafkaPayload.builder()
                .entity(entity)
                .entityId(id)
                .userId(String.valueOf(jwt.getCurrentUser().getId()))
                .username(jwt.getCurrentUser().getUsername())
                .action(KafkaPayload.Action.LOCK)
                .topic(KafkaPayload.Topic.EDIT_LOCKS)
                .build();
        editLockProducer.publishLock(payload);
    }

    @DeleteMapping("/{entity}/{id}/stop")
    public void stopEditing(@PathVariable String entity, @PathVariable Long id) {
        String key = generateKey(entity, id);
        activeLocks.remove(key);
        KafkaPayload payload = KafkaPayload.builder()
                .entity(entity)
                .entityId(id)
                .userId(String.valueOf(jwt.getCurrentUser().getId()))
                .username(jwt.getCurrentUser().getUsername())
                .action(KafkaPayload.Action.UNLOCK)
                .topic(KafkaPayload.Topic.EDIT_LOCKS)
                .build();
        editLockProducer.publishLock(payload);
    }

    @GetMapping("/{entity}/{id}")
    public ResponseEntity<String> isBeingEdited(@PathVariable String entity, @PathVariable Long id) {
        String key = generateKey(entity, id);
        return activeLocks.containsKey(key)
                ? ResponseEntity.ok(activeLocks.get(key))
                : ResponseEntity.noContent().build();
    }

    private String generateKey(String entity, Long id) {
        return entity + ":" + id;
    }
}

