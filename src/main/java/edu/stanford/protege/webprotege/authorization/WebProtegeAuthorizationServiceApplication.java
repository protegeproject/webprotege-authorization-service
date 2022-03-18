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

	@Autowired
	AccessManager accessManager;

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

	@Override
	public void run(String... args) throws Exception {
		accessManager.setAssignedRoles(Subject.forAnySignedInUser(),
									   ApplicationResource.get(),
									   Arrays.asList(
									   		BuiltInRole.PROJECT_CREATOR.getRoleId(),
											BuiltInRole.PROJECT_UPLOADER.getRoleId()
									   ));
	}
}
