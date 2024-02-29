package edu.stanford.protege.webprotege.authorization;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.security.PublicKey;
import java.util.Map;

@SpringBootTest
@ExtendWith({MongoTestExtension.class, RabbitTestExtension.class})
class WebProtegeAuthorizationServiceApplicationTests {

	@MockBean
	public Map<String, PublicKey> setUpPublicKey;

	@Test
	void contextLoads() {
	}

}
