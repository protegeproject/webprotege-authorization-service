package edu.stanford.protege.webprotege.authorization;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@ExtendWith({MongoTestExtension.class, PulsarTestExtension.class})
class WebProtegeAuthorizationServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
