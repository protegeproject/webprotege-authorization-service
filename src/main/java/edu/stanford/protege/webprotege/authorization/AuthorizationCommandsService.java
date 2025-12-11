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

    public AuthorizationCommandsService(AccessManager accessManager) {
        this.accessManager = accessManager;
    }
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
