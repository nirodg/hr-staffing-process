package org.db.hrsp.services;

import org.db.hrsp.api.config.RequestLoggingFilter;
import org.db.hrsp.service.UserService;
import org.db.hrsp.service.repository.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class UserServiceTest {

    @Autowired
    UserService userService;

    @Autowired
    RequestLoggingFilter requestLoggingFilter;

    @Test
    void save_without_concurrency() {

        User user = userService.save(createUser());
        assertEquals(0, user.getVersion());

        for (int i = 0; i < 2; i++) {
            userService.updatePosition(user.getUsername(), "newPosition " + i);
        }

        User updatedUser = userService.findByUsername(user.getUsername()).orElseThrow();
        assertAll(
                () -> assertEquals("newPosition 1", updatedUser.getPosition()),
                () -> assertEquals(2, updatedUser.getVersion())
        );

    }

    @Test
    void save_throws_optimistic_lock_exception() throws InterruptedException {

        final int nrThreads = 2;

        User user = userService.save(createUser());
        assertEquals(0, user.getVersion());

        final ExecutorService executor = Executors.newFixedThreadPool(nrThreads);
        for (int i = 0; i < nrThreads; i++) {
            executor.execute(() -> userService.updatePosition(user.getUsername(), "newPosition"));
        }

        executor.shutdown();

    }

    private static User createUser() {
        return User.builder()
                .username("technicalUser")
                .firstName("Technical")
                .lastName("User")
                .position("admin")
                .available(true)
                .build();
    }
}
