package com.example.reservation.service;

import com.example.reservation.dto.UsuarioRequest;
import com.example.reservation.dto.UsuarioResponse;
import com.example.reservation.exception.BusinessException; // Importe sua BusinessException
import com.example.reservation.exception.NotFoundException;
import com.example.reservation.model.Usuario;
import com.example.reservation.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<UsuarioResponse> listarTodos() {
        return usuarioRepository.findAll()
                .stream()
                .map(UsuarioResponse::fromEntity)
                .toList();
    }

    public UsuarioResponse buscarPorId(UUID id) {
        return UsuarioResponse.fromEntity(buscarEntidade(id));
    }

    @Transactional
    public UsuarioResponse criar(UsuarioRequest request) {
        // Regra de Negócio: E-mail único
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new BusinessException("Este e-mail já está cadastrado no sistema");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        usuario.setCargo(request.cargo());
        return UsuarioResponse.fromEntity(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse atualizar(UUID id, UsuarioRequest request) {
        Usuario usuario = buscarEntidade(id);

        // Verifica se o e-mail mudou e se o novo já existe para outro utilizador
        if (!usuario.getEmail().equals(request.email()) && usuarioRepository.existsByEmail(request.email())) {
            throw new BusinessException("O novo e-mail informado já está em uso");
        }

        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        usuario.setCargo(request.cargo());
        return UsuarioResponse.fromEntity(usuarioRepository.save(usuario));
    }

    @Transactional
    public void remover(UUID id) {
        Usuario usuario = buscarEntidade(id);
        usuarioRepository.delete(usuario);
    }

    public Usuario buscarEntidade(UUID id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
    }
}