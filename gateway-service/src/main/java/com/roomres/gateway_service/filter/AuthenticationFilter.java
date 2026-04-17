package com.roomres.gateway_service.filter;

import com.roomres.gateway_service.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private RouteValidator validator;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {

            // 1. A rota atual precisa de token? (Ignora /auth, etc)
            if (validator.isSecured.test(exchange.getRequest())) {

                // 2. Tem o Header "Authorization"?
                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    return onError(exchange.getResponse(), "Header de Autorização ausente", HttpStatus.UNAUTHORIZED);
                }

                String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    authHeader = authHeader.substring(7); // Remove a palavra "Bearer "
                } else {
                    return onError(exchange.getResponse(), "Token mal formatado", HttpStatus.UNAUTHORIZED);
                }

                try {
                    // 3. O token é válido e foi assinado pelo nosso sistema?
                    jwtUtil.validateToken(authHeader);
                } catch (Exception e) {
                    return onError(exchange.getResponse(), "Acesso negado: Token inválido ou expirado", HttpStatus.UNAUTHORIZED);
                }
            }
            // Tudo certo! Passa a requisição para frente
            return chain.filter(exchange);
        });
    }

    // Método auxiliar para barrar requisições no Spring WebFlux (Gateway)
    private reactor.core.publisher.Mono<Void> onError(ServerHttpResponse response, String err, HttpStatus httpStatus) {
        response.setStatusCode(httpStatus);
        System.out.println("GATEWAY SECURITY BLOCK: " + err);
        return response.setComplete();
    }

    public static class Config {}
}