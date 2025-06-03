package edu.stanford.protege.webprotege.authorization;

import org.keycloak.TokenVerifier;
import org.keycloak.common.VerificationException;
import org.keycloak.jose.jwk.JSONWebKeySet;
import org.keycloak.jose.jwk.JWK;
import org.keycloak.jose.jwk.JWKParser;
import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Configuration
public class TokenValidator {

    private Map<String,PublicKey> publicKeys;


    @Value("${keycloak-issuer-url}")
    private String keycloakUrl;


    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }

    @Bean
    public Map<String,PublicKey> setUpPublicKey(RestTemplate restTemplate) throws IOException, CertificateException {
        JSONWebKeySet responseEntity = restTemplate.getForObject(keycloakUrl, JSONWebKeySet.class);
        if(responseEntity != null && responseEntity.getKeys() != null && responseEntity.getKeys().length > 0) {
            // Parse the response to extract the public key
            publicKeys = new HashMap<>();
            for(JWK jwk : responseEntity.getKeys()) {
                JWKParser parser = new JWKParser(jwk);
                publicKeys.put(jwk.getKeyId(), parser.toPublicKey());
            }
        }
        return publicKeys;
    }

    /**
     * Extracts claims from a JWT token without verification.
     * Note: This method should only be used when token verification is not critical.
     * For security-critical operations, use getTokenClaims() instead.
     *
     * @param jwt The JWT token to extract claims from
     * @return Set of roles from the token's resource access
     * @throws VerificationException if the token is malformed
     */
    public Set<String> extractClaimsWithoutVerification(String jwt) throws VerificationException {
        TokenVerifier<AccessToken> verifier = TokenVerifier.create(jwt, AccessToken.class);
        AccessToken token = verifier.getToken();
        return token.getResourceAccess().get("webprotege").getRoles();
    }
}
