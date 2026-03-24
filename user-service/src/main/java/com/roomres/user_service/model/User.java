package com.roomres.user_service.model;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.validator.constraints.UUID;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    private String name;
    
    private String provider;
    
    @Column(name = "provider_id")
    private String providerId;
    
    private String role;
    
    @PrePersist
    protected void onCreate(){
        LocalDateTime createdAt = LocalDateTime.now();
        if (role == null) role = "USER";
    }

}
