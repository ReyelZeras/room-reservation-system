package com.example.reservation.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Entidade que representa o usuário que realiza reservas.
 *
 * A ideia é manter apenas dados essenciais para o contexto de reservas
 * (nome, e-mail e cargo), deixando outras responsabilidades (autenticação,
 * autorização) para serviços especializados em uma futura evolução.
 */
@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Usuario {
    private static final long serialVersionUID = 1L;

    /**
     * Identificador único do usuário.
     * Este campo é usado para equals/hashCode, garantindo que dois usuários sejam
     * considerados iguais apenas se tiverem o mesmo ID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID userID;

    /**
     * Nome completo do usuário, usado para exibição em telas e relatórios.
     */
    @Column(nullable = false)
    @NotBlank
    private String nome;

    /**
     * E-mail único para contato e identificação.
     */
    @Column(nullable = false, unique = true)
    @NotBlank
    @Email
    private String email;

    /**
     * Cargo ou função do usuário na empresa (opcional).
     */
    @Column
    private String cargo;

}
