package com.roomres.discovery_service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DiscoveryServiceApplicationTests {

	@Test
	@DisplayName("Deve carregar o contexto do Eureka Server com sucesso")
	void contextLoads() {
		// Se a aplicacao chegar aqui sem lancar excecoes,
		// significa que as configuracoes do Eureka e dependencias estao 100% corretas.
	}

}
