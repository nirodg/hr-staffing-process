package org.db.hrsp.api;

import lombok.RequiredArgsConstructor;
import org.db.hrsp.api.config.security.JwtInterceptor;
import org.db.hrsp.service.EditLockQueueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST façade for live-edit (acquire / heart-beat / release) endpoints.
 * <p>Base path: <strong>/api/editing</strong></p>
 */
@RestController
@RequestMapping("/api/editing")
@RequiredArgsConstructor
public class LiveEditController {

    private final EditLockQueueService editLockQueueService;
    private final JwtInterceptor jwt;   // supplies current user

    /* ─────────────────────────────────────── start ─────────────────────────────────────── */

    @PostMapping("/{entity}/{id}/start")
    public ResponseEntity<?> startEditing(@PathVariable String entity,
                                          @PathVariable Long id) {

        String username = jwt.getCurrentUser().getUsername();
        Long userId = jwt.getCurrentUser().getId();

        boolean acquired = editLockQueueService.tryAcquire(entity, id, username, userId);

        return acquired
                ? ResponseEntity.ok().build()
                : editLockQueueService.getCurrentEditor(entity, id)
                .map(owner -> ResponseEntity.status(409).body(owner)) // conflict + current owner
                .orElseGet(() -> ResponseEntity.status(409).build());
    }

    /* ──────────────────────────────────── heart-beat ───────────────────────────────────── */

    @PostMapping("/{entity}/{id}/touch")
    public ResponseEntity<Void> touch(@PathVariable String entity,
                                      @PathVariable Long id) {

        editLockQueueService.touch(entity, id, jwt.getCurrentUser().getUsername());
        return ResponseEntity.ok().build();
    }

    /* ────────────────────────────────────── stop ───────────────────────────────────────── */

    @DeleteMapping("/{entity}/{id}/stop")
    public ResponseEntity<Void> stopEditing(@PathVariable String entity,
                                            @PathVariable Long id) {

        String username = jwt.getCurrentUser().getUsername();
        Long userId = jwt.getCurrentUser().getId();

        editLockQueueService.release(entity, id, username, userId);
        return ResponseEntity.ok().build();
    }

    /* ───────────────────────────────────── status ──────────────────────────────────────── */

    @GetMapping("/{entity}/{id}")
    public ResponseEntity<String> currentEditor(@PathVariable String entity,
                                                @PathVariable Long id) {

        return editLockQueueService.getCurrentEditor(entity, id)
                .map(ResponseEntity::ok)          // 200 + username
                .orElseGet(() -> ResponseEntity.noContent().build()); // 204 = free
    }
}
