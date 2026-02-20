package com.example.reservation.repository;

import com.example.reservation.model.Sala;
import com.example.reservation.model.StatusSala;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SalaRepository extends JpaRepository<Sala, UUID> {

    /**
     * Busca todas as salas marcadas como ATIVA.
     * Evita replicar o filtro de status na camada de serviço.
     */
    List<Sala> findByStatus(StatusSala status);
}
