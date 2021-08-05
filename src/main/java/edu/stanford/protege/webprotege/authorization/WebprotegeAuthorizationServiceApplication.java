package edu.stanford.protege.webprotege.authorization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class WebprotegeAuthorizationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebprotegeAuthorizationServiceApplication.class, args);
	}

	@Bean
	public RoleOracleImpl getRoleOracle() {
		return RoleOracleImpl.get();
	}

	@Bean
	GetUserRolesErrorHandler getUserRolesErrorHandler() {
		return new GetUserRolesErrorHandler();
	}

}
