package com.roomres.gateway_service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GatewayServiceApplicationTests {

	@Test
	@DisplayName("Deve carregar o contexto do Spring Cloud Gateway com sucesso")

	void contextLoads() {
		// Se a aplicacao chegar aqui sem lancar excecoes,
		// significa que as rotas configuradas no application.yml sao validas e o contexto sobe corretamente.
	}

}
