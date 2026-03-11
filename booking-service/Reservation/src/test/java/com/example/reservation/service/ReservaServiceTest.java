package com.example.reservation.service;

import com.example.reservation.dto.ReservaRequest;
import com.example.reservation.exception.BusinessException;
import com.example.reservation.model.*;
import com.example.reservation.repository.ReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class ReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private SalaService salaService;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private ReservaService reservaService;

    private Sala salaAtiva;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        salaAtiva = new Sala();
        salaAtiva.setSalaID(UUID.randomUUID());
        salaAtiva.setNome("Sala Reunião 1");
        salaAtiva.setCapacidade(10);
        salaAtiva.setStatus(StatusSala.ATIVA);

        usuario = new Usuario();
        usuario.setUserID(UUID.randomUUID());
        usuario.setNome("Usuário Teste");
        usuario.setEmail("teste@example.com");
    }

    @Test
    @DisplayName("Deve criar reserva quando não há conflito e período é válido")
    void criarReserva_ComSucesso() {
        UUID salaId = salaAtiva.getSalaID();
        UUID usuarioId = usuario.getUserID();
        LocalDateTime inicio = LocalDateTime.now().plusHours(1);
        LocalDateTime fim = inicio.plusHours(1);

        when(salaService.buscarEntidade(salaId)).thenReturn(salaAtiva);
        when(usuarioService.buscarEntidade(usuarioId)).thenReturn(usuario);
        when(reservaRepository.existeConflito(eq(salaId), eq(StatusReserva.CANCELADA), any(), any()))
                .thenReturn(false);
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var request = new ReservaRequest(salaId, usuarioId, inicio, fim);

        var response = reservaService.criar(request);

        assertNotNull(response);
        assertEquals(salaId, response.salaId());
        assertEquals(usuarioId, response.usuarioId());
        assertEquals(StatusReserva.ATIVA, response.status());
    }

    @Test
    @DisplayName("Deve lançar exceção quando período é inválido (início após fim)")
    void criarReserva_PeriodoInvalido_DeveLancarExcecao() {
        UUID salaId = salaAtiva.getSalaID();
        UUID usuarioId = usuario.getUserID();
        LocalDateTime inicio = LocalDateTime.now().plusHours(2);
        LocalDateTime fim = inicio.minusHours(1);

        when(salaService.buscarEntidade(salaId)).thenReturn(salaAtiva);
        when(usuarioService.buscarEntidade(usuarioId)).thenReturn(usuario);

        var request = new ReservaRequest(salaId, usuarioId, inicio, fim);

        assertThrows(BusinessException.class, () -> reservaService.criar(request));
    }

    @Test
    @DisplayName("Deve lançar exceção quando há conflito de horário com outra reserva ativa")
    void criarReserva_ComConflito_DeveLancarExcecao() {
        UUID salaId = salaAtiva.getSalaID();
        UUID usuarioId = usuario.getUserID();
        LocalDateTime inicio = LocalDateTime.now().plusHours(1);
        LocalDateTime fim = inicio.plusHours(1);

        when(salaService.buscarEntidade(salaId)).thenReturn(salaAtiva);
        when(usuarioService.buscarEntidade(usuarioId)).thenReturn(usuario);
        when(reservaRepository.existeConflito(eq(salaId), eq(StatusReserva.CANCELADA), any(), any()))
                .thenReturn(true);

        var request = new ReservaRequest(salaId, usuarioId, inicio, fim);

        assertThrows(BusinessException.class, () -> reservaService.criar(request));
    }

    @Test
    @DisplayName("Não deve considerar reservas canceladas no conflito")
    void regraConflito_IgnoraCanceladas() {
        UUID salaId = salaAtiva.getSalaID();
        UUID usuarioId = usuario.getUserID();
        LocalDateTime inicio = LocalDateTime.now().plusHours(1);
        LocalDateTime fim = inicio.plusHours(1);

        when(salaService.buscarEntidade(salaId)).thenReturn(salaAtiva);
        when(usuarioService.buscarEntidade(usuarioId)).thenReturn(usuario);
        // simulando que não há conflito quando ignoramos CANCELADA
        when(reservaRepository.existeConflito(eq(salaId), eq(StatusReserva.CANCELADA), any(), any()))
                .thenReturn(false);
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var request = new ReservaRequest(salaId, usuarioId, inicio, fim);

        assertDoesNotThrow(() -> reservaService.criar(request));
    }

    @Test
    @DisplayName("Regra de intervalo semiaberto permite reserva com início igual ao fim de outra")
    void regraIntervaloSemiaberto_Bordas() {
        UUID salaId = salaAtiva.getSalaID();
        UUID usuarioId = usuario.getUserID();

        // Reserva existente: 10h–11h
        LocalDateTime inicioExistente = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime fimExistente = LocalDateTime.of(2025, 1, 1, 11, 0);

        // Nova reserva: 11h–12h (não deveria conflitar)
        LocalDateTime inicioNova = fimExistente;
        LocalDateTime fimNova = inicioNova.plusHours(1);

        when(salaService.buscarEntidade(salaId)).thenReturn(salaAtiva);
        when(usuarioService.buscarEntidade(usuarioId)).thenReturn(usuario);

        // simulando que o repositório não identificou conflito para este cenário de borda
        when(reservaRepository.existeConflito(eq(salaId), eq(StatusReserva.CANCELADA),
                eq(inicioNova), eq(fimNova)))
                .thenReturn(false);
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var request = new ReservaRequest(salaId, usuarioId, inicioNova, fimNova);

        assertDoesNotThrow(() -> reservaService.criar(request));
    }
}

