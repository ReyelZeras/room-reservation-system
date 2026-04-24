package com.roomres.gateway_service.filter;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {

    // Lista de endpoints PÚBLICOS que o Gateway deixará passar sem exigir o Token JWT
    public static final List<String> openApiEndpoints = List.of(
            "/api/v1/auth",
            "/api/v1/notifications/stream", // ADICIONADO: Libera a conexão SSE nativa do navegador
            "/eureka",
            "/v3/api-docs",
            "/swagger-ui",
            "/api/v1/suggestions"
    );

    // Função que verifica se o caminho atual da requisição PRECISA ser validado
    // Se o caminho estiver na lista acima, ele retorna FALSE (isSecured = false).
    public Predicate<ServerHttpRequest> isSecured =
            request -> openApiEndpoints
                    .stream()
                    .noneMatch(uri -> request.getURI().getPath().contains(uri));
}