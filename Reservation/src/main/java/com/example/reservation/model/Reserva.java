package com.example.reservation.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidade que representa uma reserva de uma sala por um usuário.
 *
 * Utilizei intervalo semiaberto para representar o período da reserva:
 * [dataHoraInicio, dataHoraFim), ou seja, inclui o início mas não inclui o fim.
 * Isso significa que:
 * - Reserva A: 10h–11h e Reserva B: 11h–12h NÃO conflitam;
 * - Reserva A: 10h–11h e Reserva B: 10h30–11h30 conflitam.
 */
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Getter
@Setter
public class Reserva {
    private static final long serialVersionUID = 1L;

    /**
     * Identificador único da reserva.
     * Este campo é usado para equals/hashCode, garantindo que duas reservas sejam
     * consideradas iguais apenas se tiverem o mesmo ID.
     * Não incluímos relacionamentos (sala, usuario) para evitar problemas com lazy loading.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID reservaID;

    /**
     * Data/hora de início da reserva (incluída no intervalo).
     */
    @NotNull
    private LocalDateTime dataHoraInicio;

    /**
     * Data/hora de fim da reserva (NÃO incluída no intervalo).
     */
    @NotNull
    private LocalDateTime dataHoraFim;

    /**
     * Usuário que realizou a reserva.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    /**
     * Sala reservada.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sala_id", nullable = false)
    private Sala sala;

    /**
     * Status atual da reserva.
     * Reservas CANCELADAS não participam de checagem de conflito.
     */
    @Enumerated(EnumType.STRING)
    private StatusReserva status;

    /**
     * Regra de domínio: data/hora de início deve ser anterior à de fim.
     */
    public boolean periodoValido() {
        return dataHoraInicio != null
                && dataHoraFim != null
                && dataHoraInicio.isBefore(dataHoraFim);
    }

    /**
     * Indica se a reserva está ativa.
     */
    public boolean isAtiva() {
        return StatusReserva.ATIVA.equals(this.status);
    }

    /**
     * Marca a reserva como cancelada.
     * Não removemos o registro para manter histórico.
     */
    public void cancelar() {
        this.status = StatusReserva.CANCELADA;
    }




}
