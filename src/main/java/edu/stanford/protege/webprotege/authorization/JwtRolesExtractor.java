package edu.stanford.protege.webprotege.authorization;

import org.keycloak.TokenVerifier;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

@Component
public class JwtRolesExtractor {

    private static final Logger logger = LoggerFactory.getLogger(JwtRolesExtractor.class);

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

    /**
     * Extracts the roles from the specified JWT, which can be null.
     * @param jwt The JWT. This may be null and may be blank/empty
     * @return The roles extracted from the JWT. If there was a problem extracting the
     * roles then the empty set will be returned and no exception will be thrown.
     */
    public Set<String> safeExtractRolesWithoutVerification(String jwt) {
        if(jwt == null) {
            logger.debug("JWT is null. Returning empty set of roles.");
            return Collections.emptySet();
        }
        if(jwt.isBlank()) {
            logger.debug("JWT is empty. Returning empty set of roles.");
            return Collections.emptySet();
        }
        try {
            return extractRolesWithoutVerification(jwt);
        } catch(VerificationException e) {
            logger.error("Error extracting roles from JWT. Returning empty set of roles." , e);
            return Collections.emptySet();
        }
    }
}
