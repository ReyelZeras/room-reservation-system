package com.example.reservation.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
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
public class Sala {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID salaID;
    @NotBlank
    private String nome;
    @Column(nullable = false)
    @Min(1)
    private Integer capacidade;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusSala status;
    private String localizacao;

}
