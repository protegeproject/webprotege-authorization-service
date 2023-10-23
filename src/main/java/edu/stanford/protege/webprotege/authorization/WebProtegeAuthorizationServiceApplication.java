package edu.stanford.protege.webprotege.authorization;

import edu.stanford.protege.webprotege.ipc.WebProtegeIpcApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.Arrays;

@SpringBootApplication
@Import(WebProtegeIpcApplication.class)
public class WebProtegeAuthorizationServiceApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(WebProtegeAuthorizationServiceApplication.class, args);
	}

	@Bean
	public RoleOracleImpl getRoleOracle() {
		return RoleOracleImpl.get();
	}

	@Override
	public void run(String... args) {
	}
}
