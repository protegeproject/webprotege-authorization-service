package edu.stanford.protege.webprotege.authorization;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.TokenVerifier;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtRolesExtractor_Tests {

    @Test
    void extractRolesWithoutVerification_shouldReturnRolesFromToken() throws VerificationException {
        var jwt = "some-jwt";
        var expectedRoles = Set.of("role1" , "role2" );
        var webProtegeAccess = mock(AccessToken.Access.class);
        when(webProtegeAccess.getRoles()).thenReturn(expectedRoles);

        var token = mock(AccessToken.class);
        when(token.getResourceAccess()).thenReturn(Map.of("webprotege" , webProtegeAccess));

        var verifier = mock(TokenVerifier.class);
        when(verifier.getToken()).thenReturn(token);

        try(var tokenVerifierStatic = mockStatic(TokenVerifier.class)) {
            tokenVerifierStatic.when(() -> TokenVerifier.create(jwt, AccessToken.class))
                    .thenReturn(verifier);

            var extractor = new JwtRolesExtractor();

            var roles = extractor.extractRolesWithoutVerification(jwt);

            assertEquals(expectedRoles, roles);
        }
    }

    @Test
    void extractRolesWithoutVerification_shouldPropagateVerificationException() throws VerificationException {
        var jwt = "bad-jwt";

        var verifier = mock(TokenVerifier.class);
        when(verifier.getToken()).thenThrow(new VerificationException("Malformed token" ));

        try(var tokenVerifierStatic = mockStatic(TokenVerifier.class)) {
            tokenVerifierStatic.when(() -> TokenVerifier.create(jwt, AccessToken.class))
                    .thenReturn(verifier);

            var extractor = new JwtRolesExtractor();

            assertThrows(VerificationException.class,
                    () -> extractor.extractRolesWithoutVerification(jwt));
        }
    }

    @Test
    void safeExtractRolesWithoutVerification_nullJwt_returnsEmptySet() {
        var extractor = new JwtRolesExtractor();

        var roles = extractor.safeExtractRolesWithoutVerification(null);

        assertEquals(Collections.emptySet(), roles);
    }

    @Test
    void safeExtractRolesWithoutVerification_blankJwt_returnsEmptySet() {
        var extractor = new JwtRolesExtractor();

        var roles = extractor.safeExtractRolesWithoutVerification("   " );

        assertEquals(Collections.emptySet(), roles);
    }

    @Test
    void safeExtractRolesWithoutVerification_validJwt_returnsRoles() throws VerificationException {
        var jwt = "some-jwt";

        var expectedRoles = Set.of("roleA" , "roleB" );

        var extractor = spy(new JwtRolesExtractor());
        doReturn(expectedRoles).when(extractor).extractRolesWithoutVerification(jwt);

        var roles = extractor.safeExtractRolesWithoutVerification(jwt);

        assertEquals(expectedRoles, roles);
    }

    @Test
    void safeExtractRolesWithoutVerification_whenExtractionThrowsVerificationException_returnsEmptySet() throws VerificationException {
        var jwt = "bad-jwt";

        var extractor = spy(new JwtRolesExtractor());
        doThrow(new VerificationException("Bad token" ))
                .when(extractor)
                .extractRolesWithoutVerification(jwt);

        var roles = extractor.safeExtractRolesWithoutVerification(jwt);

        assertEquals(Collections.emptySet(), roles);
    }
}
