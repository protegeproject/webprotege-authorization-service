package edu.stanford.protege.webprotege.authorization;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@ExtendWith(MongoTestExtension.class)
class WebProtegeAuthorizationServiceApplicationTests extends IntegrationTestsExtension{

	@Test
	void contextLoads() {
	}

}
