package com.example.reservation.model;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
public class Sala {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID salaID;
    private String nome;
    private Integer capacidade;
    @Enumerated(EnumType.STRING)
    private StatusSala status;
    private String localizacao;

}
