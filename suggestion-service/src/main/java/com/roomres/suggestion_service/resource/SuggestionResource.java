package com.roomres.suggestion_service.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/api/v1/suggestions")
public class SuggestionResource {

    // Simulação de um motor ultrarrápido de recomendação estática
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Suggestion> getTopSuggestions() {
        return List.of(
                new Suggestion("Sala de Reuniões Alpha", "Ideal para equipas pequenas", 4),
                new Suggestion("Auditório Master", "Equipado com projetor 4K", 50),
                new Suggestion("Laboratório de Ideias", "Quadro branco em todas as paredes", 10)
        );
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