package edu.stanford.protege.webprotege.authorization;

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

    private final BuiltInRoleOracle builtInRoleOracle;

    public AuthorizationCommandsService(AccessManager accessManager, TokenValidator tokenValidator, BuiltInRoleOracle builtInRoleOracle) {
        this.accessManager = accessManager;
        this.tokenValidator = tokenValidator;
        this.builtInRoleOracle = builtInRoleOracle;
    }
    // TODO:  Update this when Alex has committed the code
    public GetAuthorizationStatusResponse handleAuthorizationStatusCommand(GetAuthorizationStatusRequest request, ExecutionContext executionContext) {
        var hasPermission = accessManager.hasPermission(request.subject(),
                    request.resource(),
                    request.capability(),
                executionContext.jwt());

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
