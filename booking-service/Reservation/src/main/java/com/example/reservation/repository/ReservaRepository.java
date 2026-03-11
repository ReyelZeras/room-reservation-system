package com.example.reservation.repository;

import com.example.reservation.model.Reserva;
import com.example.reservation.model.StatusReserva;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, UUID> {

    /**
     * Verifica se existe conflito de horário para uma determinada sala, considerando
     * intervalo semiaberto [dataInicio, dataFim).
     *
     * Regra: há conflito quando o novo intervalo sobrepõe algum existente:
     * (:dataInicio < r.dataHoraFim) AND (:dataFim > r.dataHoraInicio)
     * Reservas com status igual a {@code statusIgnorado} (por exemplo, CANCELADA)
     * são desconsideradas.
     */
    @Query("""
            SELECT COUNT(r) > 0 FROM Reserva r
             WHERE r.sala.salaID = :salaId
               AND r.status <> :statusIgnorado
               AND :dataInicio < r.dataHoraFim
               AND :dataFim > r.dataHoraInicio
            """)
    boolean existeConflito(@Param("salaId") UUID salaId,
                           @Param("statusIgnorado") StatusReserva statusIgnorado,
                           @Param("dataInicio") LocalDateTime dataInicio,
                           @Param("dataFim") LocalDateTime dataFim);

    /**
     * Lista reservas de uma sala em um período, com paginação, preparando
     * para telas de listagem e relatórios.
     */
    @Query("""
            SELECT r FROM Reserva r
             WHERE r.sala.salaID = :salaId
               AND r.dataHoraInicio >= :dataInicio
               AND r.dataHoraFim   <= :dataFim
            """)
    Page<Reserva> findBySalaAndPeriodo(@Param("salaId") UUID salaId,
                                       @Param("dataInicio") LocalDateTime dataInicio,
                                       @Param("dataFim") LocalDateTime dataFim,
                                       Pageable pageable);
}
