package com.example.reservation.service;

import com.example.reservation.dto.ReservaRequest;
import com.example.reservation.dto.ReservaResponse;
import com.example.reservation.exception.BusinessException;
import com.example.reservation.exception.NotFoundException;
import com.example.reservation.model.Reserva;
import com.example.reservation.model.StatusReserva;
import com.example.reservation.model.Usuario;
import com.example.reservation.model.Sala;
import com.example.reservation.repository.ReservaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final SalaService salaService;
    private final UsuarioService usuarioService;

    public ReservaService(ReservaRepository reservaRepository,
                          SalaService salaService,
                          UsuarioService usuarioService) {
        this.reservaRepository = reservaRepository;
        this.salaService = salaService;
        this.usuarioService = usuarioService;
    }

    public Page<ReservaResponse> listar(Pageable pageable) {
        return reservaRepository.findAll(pageable)
                .map(ReservaResponse::fromEntity);
    }

    public ReservaResponse buscarPorId(UUID id) {
        return ReservaResponse.fromEntity(buscarEntidade(id));
    }

    @Transactional
    public ReservaResponse criar(ReservaRequest request) {
        Sala sala = salaService.buscarEntidade(request.salaId());
        Usuario usuario = usuarioService.buscarEntidade(request.usuarioId());

        if (!sala.isAtiva()) {
            throw new BusinessException("Não é possível reservar uma sala inativa");
        }

        Reserva reserva = new Reserva();
        reserva.setSala(sala);
        reserva.setUsuario(usuario);
        reserva.setDataHoraInicio(request.dataHoraInicio());
        reserva.setDataHoraFim(request.dataHoraFim());
        reserva.setStatus(StatusReserva.ATIVA);

        if (!reserva.periodoValido()) {
            throw new BusinessException("Data/hora de início deve ser anterior à data/hora de fim");
        }

        validarConflito(sala.getSalaID(), reserva.getDataHoraInicio(), reserva.getDataHoraFim());

        return ReservaResponse.fromEntity(reservaRepository.save(reserva));
    }

    @Transactional
    public ReservaResponse cancelar(UUID id) {
        Reserva reserva = buscarEntidade(id);
        if (!reserva.isAtiva()) {
            return ReservaResponse.fromEntity(reserva);
        }
        reserva.cancelar();
        return ReservaResponse.fromEntity(reservaRepository.save(reserva));
    }

    public Reserva buscarEntidade(UUID id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Reserva não encontrada"));
    }

    private void validarConflito(UUID salaId, LocalDateTime inicio, LocalDateTime fim) {
        boolean existeConflito = reservaRepository.existeConflito(
                salaId,
                StatusReserva.CANCELADA,
                inicio,
                fim
        );
        if (existeConflito) {
            throw new BusinessException("Já existe reserva para esta sala no período informado");
        }
    }
}

