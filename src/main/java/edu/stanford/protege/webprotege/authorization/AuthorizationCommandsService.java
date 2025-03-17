package edu.stanford.protege.webprotege.authorization;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import org.keycloak.common.VerificationException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AuthorizationCommandsService {

    private final AccessManager accessManager;

    private final TokenValidator tokenValidator;

    private final RoleOracle roleOracle;

    public AuthorizationCommandsService(AccessManager accessManager, TokenValidator tokenValidator, RoleOracle roleOracle) {
        this.accessManager = accessManager;
        this.tokenValidator = tokenValidator;
        this.roleOracle = roleOracle;
    }

    public GetAuthorizationStatusResponse handleAuthorizationStatusCommand(GetAuthorizationStatusRequest request, ExecutionContext executionContext) {
        var hasPermission = false;
        if(request.resource().isApplication()) {
            List<RoleId> roleIds;
            try {
                roleIds = tokenValidator.getTokenClaims(executionContext.jwt()).stream()
                        .map(RoleId::new)
                        .toList();
                Set<Capability> capabilities  = new HashSet<>(roleOracle.getCapabilitiesAssociatedToRoles(roleIds));
                hasPermission = capabilities.contains(request.capability());

            } catch (VerificationException e) {
                throw new RuntimeException(e);
            }

        }else {
            hasPermission = accessManager.hasPermission(request.subject(),
                    request.resource(),
                    request.capability());
        }
        if(hasPermission) {
            return new GetAuthorizationStatusResponse(request.resource(),
                    request.subject(),
                    AuthorizationStatus.AUTHORIZED);
        }
        else {
            return new GetAuthorizationStatusResponse(request.resource(),
                    request.subject(),
                    AuthorizationStatus.UNAUTHORIZED);
        }
    }
}
