package org.db.hrsp.service.repository;

import org.db.hrsp.service.repository.model.Role;
import org.db.hrsp.service.repository.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

//    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE LOWER(r) = LOWER(:role)")
    long countByRoles(Role role);

}
