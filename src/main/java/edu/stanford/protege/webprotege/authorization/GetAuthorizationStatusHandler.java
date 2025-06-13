package edu.stanford.protege.webprotege.authorization;

import edu.stanford.protege.webprotege.ipc.CommandHandler;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import edu.stanford.protege.webprotege.ipc.WebProtegeHandler;
import org.keycloak.common.VerificationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2021-08-09
 */
@WebProtegeHandler
public class GetAuthorizationStatusHandler implements CommandHandler<GetAuthorizationStatusRequest, GetAuthorizationStatusResponse> {
    private final static Logger logger = LoggerFactory.getLogger(GetAuthorizedCapabilitiesHandler.class);

    private final AccessManager accessManager;

    private final TokenValidator tokenValidator;

    private final BuiltInRoleOracle builtInRoleOracle;

    public GetAuthorizationStatusHandler(AccessManager accessManager, TokenValidator tokenValidator, BuiltInRoleOracle builtInRoleOracle) {
        this.accessManager = accessManager;
        this.tokenValidator = tokenValidator;
        this.builtInRoleOracle = builtInRoleOracle;
    }

    @Nonnull
    @Override
    public String getChannelName() {
        return GetAuthorizationStatusRequest.CHANNEL;
    }

    @Override
    public Class<GetAuthorizationStatusRequest> getRequestClass() {
        return GetAuthorizationStatusRequest.class;
    }

    @Override
    public Mono<GetAuthorizationStatusResponse> handleRequest(GetAuthorizationStatusRequest request, ExecutionContext executionContext) {
        var hasPermission = false;
        if(request.resource().isApplication()) {
            List<RoleId> roleIds;
            try {
                roleIds = tokenValidator.extractClaimsWithoutVerification(executionContext.jwt()).stream()
                        .map(RoleId::new)
                        .toList();
                Set<Capability> capabilities  = new HashSet<>(builtInRoleOracle.getCapabilitiesAssociatedToRoles(roleIds));
                hasPermission = capabilities.contains(request.capability());

            } catch (VerificationException e) {
                logger.error("Error getting token claims", e);
                throw new RuntimeException(e);
            }

        }else {
            hasPermission = accessManager.hasPermission(request.subject(),
                    request.resource(),
                    request.capability());
        }
        if(hasPermission) {
            return Mono.just(new GetAuthorizationStatusResponse(request.resource(),
                    request.subject(),
                    AuthorizationStatus.AUTHORIZED));
        }
        else {
            return Mono.just(new GetAuthorizationStatusResponse(request.resource(),
                    request.subject(),
                    AuthorizationStatus.UNAUTHORIZED));
        }
    }
}
