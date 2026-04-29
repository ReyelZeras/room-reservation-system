package com.roomres.suggestion_service;

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
                .statusCode(200);
        // Removido o teste de tamanho estrito (body("size()", is(3))),
        // pois agora os dados vêm do banco de dados real via REST Client e a quantidade é dinâmica.
    }
}