package com.roomres.booking_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;

// Removida a URL fixa. O nome "user-service" será resolvido pelo Eureka/LoadBalancer
@FeignClient(name = "user-service")
public interface UserClient {
    @GetMapping("/api/v1/users/{id}")
    Object getUserById(@PathVariable("id") UUID id);
}