package com.example.reservation.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Entidade que representa uma sala física que pode ser reservada.
 *
 * Regras principais de domínio:
 * - capacidade deve ser sempre positiva;
 * - somente salas com status ATIVA podem ser reservadas.
 */
@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Sala {
    private static final long serialVersionUID = 1L;

    /**
     * Identificador único da sala.
     * Usamos UUID para evitar colisões entre ambientes e facilitar integrações.
     * Este campo é usado para equals/hashCode, garantindo que duas salas sejam
     * consideradas iguais apenas se tiverem o mesmo ID (mesmo que ainda não persistido).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID salaID;

    /**
     * Nome amigável da sala (ex.: "Reunião 1", "Auditório").
     */
    @NotBlank
    private String nome;

    /**
     * Capacidade máxima de pessoas que a sala suporta.
     * Regra de domínio: não pode ser menor que 1.
     */
    @Column(nullable = false)
    @Min(1)
    private Integer capacidade;

    /**
     * Status atual da sala. Apenas salas ATIVAS podem receber novas reservas.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusSala status;

    /**
     * Descrição opcional de localização (andar, prédio, etc.).
     */
    private String localizacao;

    /**
     * Verifica se a sala está ativa para uso.
     * Encapsula a regra de negócio para evitar checagens diretas do enum
     * espalhadas pelo código.
     */
    public boolean isAtiva() {
        return StatusSala.ATIVA.equals(this.status);
    }

}
