package com.roomres.suggestion_service.resource;

import com.roomres.suggestion_service.client.RoomRestClient;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Collectors;

@Path("/api/v1/suggestions")
@Tag(name = "Suggestions", description = "Motor de recomendações de altíssima performance")
public class SuggestionResource {

    // 🚀 Injeta o Cliente REST
    @RestClient
    RoomRestClient roomClient;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Lista sugestões de salas", description = "Consome o room-service para recomendar as 3 maiores salas disponíveis no sistema real.")
    public List<Suggestion> getTopSuggestions() {
        try {
            // Comunicação Microservice-to-Microservice via Quarkus REST Client
            List<RoomRestClient.RoomDTO> allRooms = roomClient.getAllRooms();

            // Filtra as DISPONÍVEIS, ordena pelas MAIORES, e apanha apenas 3.
            return allRooms.stream()
                    .filter(r -> "AVAILABLE".equals(r.status))
                    .sorted((r1, r2) -> Integer.compare(r2.capacity, r1.capacity))
                    .limit(3)
                    .map(r -> new Suggestion(
                            r.name,
                            "Localização: " + (r.location != null ? r.location : "Não informada"),
                            r.capacity))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            // Fallback de Segurança (Caso o room-service do Spring esteja desligado)
            return List.of(
                    new Suggestion("Sistema de Sugestão Indisponível", "Por favor, aceda ao catálogo completo.", 0)
            );
        }
    }

    // Classe de modelo interna apenas para o retorno (DTO)
    public static class Suggestion {
        public String roomName;
        public String description;
        public int capacity;

        public Suggestion(String roomName, String description, int capacity) {
            this.roomName = roomName;
            this.description = description;
            this.capacity = capacity;
        }
    }
}