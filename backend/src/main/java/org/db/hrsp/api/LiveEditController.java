package org.db.hrsp.api;

import lombok.RequiredArgsConstructor;
import org.db.hrsp.api.config.security.JwtInterceptor;
import org.db.hrsp.service.EditLockQueueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/editing")
@RequiredArgsConstructor
public class LiveEditController {

    private final EditLockQueueService editLockQueueService;
    private final JwtInterceptor jwt;

    @PostMapping("/{entity}/{id}/start")
    public ResponseEntity<?> startEditing(@PathVariable("entity") String entity, @PathVariable("id") Long id) {
        String username = jwt.getCurrentUser().getUsername();
        Long userId = jwt.getCurrentUser().getId();

        boolean acquired = editLockQueueService.tryAcquireLock(entity, id, username, userId);
        if (acquired) {
            return ResponseEntity.ok().build();
        } else {
            return editLockQueueService.getCurrentEditor(entity, id)
                    .map(editor -> ResponseEntity.status(409).body(editor))
                    .orElseGet(() -> ResponseEntity.status(409).build());
        }
    }

    @DeleteMapping("/{entity}/{id}/stop")
    public ResponseEntity<?> stopEditing(@PathVariable String entity, @PathVariable Long id) {
        String username = jwt.getCurrentUser().getUsername();
        Long userId = jwt.getCurrentUser().getId();

        editLockQueueService.releaseLock(entity, id, username, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{entity}/{id}")
    public ResponseEntity<String> isBeingEdited(@PathVariable String entity, @PathVariable Long id) {
        return editLockQueueService.getCurrentEditor(entity, id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    private String generateKey(String entity, Long id) {
        return entity + ":" + id;
    }

}