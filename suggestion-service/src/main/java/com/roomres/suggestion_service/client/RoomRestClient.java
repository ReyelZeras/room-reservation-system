package com.roomres.suggestion_service.client;


import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import java.util.List;


// Registra este cliente ligando-o à URL "room-api" que definimos no application.properties
@RegisterRestClient(configKey = "room-api")
@Path("/api/v1/rooms")
public interface RoomRestClient {


    @GET
    List<RoomDTO> getAllRooms();


    // DTO Interno para receber os dados do Spring Boot
    class RoomDTO {
        public String id;
        public String name;
        public int capacity;
        public String location;
        public String status;
    }
}

