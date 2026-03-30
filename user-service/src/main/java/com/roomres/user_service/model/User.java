package com.roomres.user_service.model;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE users SET active = false WHERE id=?")
@SQLRestriction("active = true")
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

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder.Default
    private boolean active = true;
    
    @PrePersist
    protected void onCreate(){
        this.createdAt = LocalDateTime.now();
        if (role == null) role = "USER";
    }

}
