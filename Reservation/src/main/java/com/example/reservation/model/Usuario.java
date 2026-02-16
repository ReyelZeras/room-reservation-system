package com.example.reservation.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Usuario {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID userID;
    @Column(nullable = false)
    @NotBlank
    private String nome;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = true)
    private String cargo;


}
