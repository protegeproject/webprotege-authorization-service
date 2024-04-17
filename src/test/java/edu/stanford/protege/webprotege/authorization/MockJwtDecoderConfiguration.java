package edu.stanford.protege.webprotege.authorization;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.JwtDecoder;


/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2024-03-26
 */
@TestConfiguration
public class MockJwtDecoderConfiguration {

    @Bean
    JwtDecoder jwtDecoder() {
        return s -> null;
    }
}

