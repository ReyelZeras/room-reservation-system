package com.roomres.user_service.repository;

import com.roomres.user_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByProviderId(String providerId);

    // NOVO MÉTODO: Permite verificar se um username já está em uso
    Optional<User> findByUsername(String username);
}