package com.roomres.suggestion_service.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class SuggestionResourceTest {

    // Arquitetura: Diferente do Spring Boot, o Quarkus sobe o contexto de testes em milissegundos.
    // Por isso, é comum usar o REST Assured para testar os Controllers como se fôssemos o Postman.

    @Test
    @DisplayName("Deve retornar a lista de sugestões com status 200 OK")
    void testGetSuggestionsEndpoint() {
        given()
                .when().get("/api/v1/suggestions")
                .then()
                .statusCode(200)
                .body("size()", is(3)); // Valida se a lista estática possui os 3 itens configurados
    }
}