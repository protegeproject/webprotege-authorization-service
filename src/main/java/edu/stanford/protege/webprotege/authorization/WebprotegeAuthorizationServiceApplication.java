package edu.stanford.protege.webprotege.authorization;

import edu.stanford.protege.webprotege.ipc.WebProtegeIpcApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.List;

@SpringBootApplication
@Import(WebProtegeIpcApplication.class)
public class WebProtegeAuthorizationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebProtegeAuthorizationServiceApplication.class, args);
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
