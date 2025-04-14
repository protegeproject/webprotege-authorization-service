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
public class GetAuthorizedActionsHandler implements CommandHandler<GetAuthorizedCapabilitiesRequest, GetAuthorizedCapabilitiesResponse> {
    private final static Logger logger = LoggerFactory.getLogger(GetAuthorizedActionsHandler.class);

    private final AccessManager accessManager;
    private final TokenValidator tokenValidator;

    private final BuiltInRoleOracle builtInRoleOracle;

    public GetAuthorizedActionsHandler(AccessManager accessManager, TokenValidator tokenValidator, BuiltInRoleOracle builtInRoleOracle) {
        this.accessManager = accessManager;
        this.tokenValidator = tokenValidator;
        this.builtInRoleOracle = builtInRoleOracle;
    }

    @Nonnull
    @Override
    public String getChannelName() {
        return GetAuthorizedCapabilitiesRequest.CHANNEL;
    }

    @Override
    public Class<GetAuthorizedCapabilitiesRequest> getRequestClass() {
        return GetAuthorizedCapabilitiesRequest.class;
    }

    @Override
    public Mono<GetAuthorizedCapabilitiesResponse> handleRequest(GetAuthorizedCapabilitiesRequest request, ExecutionContext executionContext) {

        if(request.resource().isApplication()) {
            try {
                List<RoleId> roleIds = tokenValidator.getTokenClaims(executionContext.jwt()).stream()
                        .map(RoleId::new)
                        .toList();
                Set<Capability> capabilities  = new HashSet<>(builtInRoleOracle.getCapabilitiesAssociatedToRoles(roleIds));
                return Mono.just(new GetAuthorizedCapabilitiesResponse(request.resource(),
                        request.subject(),
                        capabilities));

            } catch (VerificationException e) {
                throw new RuntimeException(e);
            }
        }else {
            var capabilityClosure = accessManager.getCapabilityClosure(request.subject(),
                    request.resource());
            logger.info("Retrieved capabilities for {}.  Capabilities: {}", request.subject(), capabilityClosure);
            return Mono.just(new GetAuthorizedCapabilitiesResponse(request.resource(),
                    request.subject(),
                    capabilityClosure));
        }
    }
}
