package edu.stanford.protege.webprotege.authorization;

import edu.stanford.protege.webprotege.ipc.WebProtegeIpcApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(WebProtegeIpcApplication.class)
public class WebProtegeAuthorizationServiceApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(WebProtegeAuthorizationServiceApplication.class, args);
	}

	@Bean
	public BuiltInRoleOracleImpl getRoleOracle() {
		return BuiltInRoleOracleImpl.get();
	}

	@Override
	public void run(String... args) {
	}
}
