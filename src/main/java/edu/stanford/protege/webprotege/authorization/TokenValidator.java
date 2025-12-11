package edu.stanford.protege.webprotege.authorization;

import org.keycloak.TokenVerifier;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;
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

    /**
     * Extracts the roles from a JWT token without verification.
     * Note: This method should only be used when token verification is not critical.
     * For security-critical operations, use getTokenClaims() instead.
     *
     * @param jwt The JWT token to extract claims from
     * @return Set of roles from the token's resource access
     * @throws VerificationException if the token is malformed
     */
    public Set<String> extractRolesWithoutVerification(String jwt) throws VerificationException {
        TokenVerifier<AccessToken> verifier = TokenVerifier.create(jwt, AccessToken.class);
        AccessToken token = verifier.getToken();
        return token.getResourceAccess().get("webprotege").getRoles();
    }
}
