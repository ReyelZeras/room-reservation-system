package com.roomres.booking_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;

// Aponta para o User Service na porta 8081
@FeignClient(name = "user-service", url = "http://localhost:8081/api/v1/users")
public interface UserClient {

    @GetMapping("/{id}")
    Object getUserById(@PathVariable("id") UUID id);
}