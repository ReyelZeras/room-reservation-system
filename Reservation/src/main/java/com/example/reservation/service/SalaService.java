package com.example.reservation.service;

import com.example.reservation.dto.SalaRequest;
import com.example.reservation.dto.SalaResponse;
import com.example.reservation.exception.NotFoundException;
import com.example.reservation.model.Sala;
import com.example.reservation.model.StatusSala;
import com.example.reservation.repository.SalaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SalaService {

    private final SalaRepository salaRepository;

    public SalaService(SalaRepository salaRepository) {
        this.salaRepository = salaRepository;
    }

    public List<SalaResponse> listarTodas() {
        return salaRepository.findAll()
                .stream()
                .map(SalaResponse::fromEntity)
                .toList();
    }

    public List<SalaResponse> listarAtivas() {
        return salaRepository.findByStatus(StatusSala.ATIVA)
                .stream()
                .map(SalaResponse::fromEntity)
                .toList();
    }

    public SalaResponse buscarPorId(UUID id) {
        Sala sala = buscarEntidade(id);
        return SalaResponse.fromEntity(sala);
    }

    @Transactional
    public SalaResponse criar(SalaRequest request) {
        Sala sala = new Sala();
        sala.setNome(request.nome());
        sala.setCapacidade(request.capacidade());
        sala.setStatus(request.status());
        sala.setLocalizacao(request.localizacao());
        return SalaResponse.fromEntity(salaRepository.save(sala));
    }

    @Transactional
    public SalaResponse atualizar(UUID id, SalaRequest request) {
        Sala sala = buscarEntidade(id);
        sala.setNome(request.nome());
        sala.setCapacidade(request.capacidade());
        sala.setStatus(request.status());
        sala.setLocalizacao(request.localizacao());
        return SalaResponse.fromEntity(salaRepository.save(sala));
    }

    /**
     * Regra de negócio simples: ao invés de excluir fisicamente, podemos
     * apenas inativar a sala. Aqui, para simplificar, removemos o registro.
     */
    @Transactional
    public void remover(UUID id) {
        Sala sala = buscarEntidade(id);
        salaRepository.delete(sala);
    }

    public Sala buscarEntidade(UUID id) {
        return salaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Sala não encontrada"));
    }
}

