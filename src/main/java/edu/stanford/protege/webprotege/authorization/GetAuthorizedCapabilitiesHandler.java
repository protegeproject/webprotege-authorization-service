package edu.stanford.protege.webprotege.authorization;

import edu.stanford.protege.webprotege.ipc.CommandHandler;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import edu.stanford.protege.webprotege.ipc.WebProtegeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.HashSet;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2021-08-09
 */
@WebProtegeHandler
public class GetAuthorizedCapabilitiesHandler implements CommandHandler<GetAuthorizedCapabilitiesRequest, GetAuthorizedCapabilitiesResponse> {
    private final static Logger logger = LoggerFactory.getLogger(GetAuthorizedCapabilitiesHandler.class);

    private final AccessManager accessManager;

    private final JwtRolesExtractor jwtRolesExtractor;

    private final BuiltInRoleOracle builtInRoleOracle;

    public GetAuthorizedCapabilitiesHandler(AccessManager accessManager, JwtRolesExtractor jwtRolesExtractor, BuiltInRoleOracle builtInRoleOracle) {
        this.accessManager = accessManager;
        this.jwtRolesExtractor = jwtRolesExtractor;
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
        var capabilities = new HashSet<Capability>();

        // We should be able to change this to check if the resource is the application.
        // extract any SUPER admin capabilities from token
        var roleIds = jwtRolesExtractor.safeExtractRolesWithoutVerification(executionContext.jwt()).stream()
                .map(RoleId::new)
                .toList();
        capabilities.addAll(new HashSet<>(builtInRoleOracle.getCapabilitiesAssociatedToRoles(roleIds)));

        capabilities.addAll(accessManager.getCapabilityClosure(request.subject(),
                request.resource()));

        return Mono.just(new GetAuthorizedCapabilitiesResponse(request.resource(),
                request.subject(),
                capabilities));

    }
}
