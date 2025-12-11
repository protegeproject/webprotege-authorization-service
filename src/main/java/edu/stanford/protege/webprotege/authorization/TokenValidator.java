package edu.stanford.protege.webprotege.authorization;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.Set;

@Configuration
public class TokenValidator {

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
}
