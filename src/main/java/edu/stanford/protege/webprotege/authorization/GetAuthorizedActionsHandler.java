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
public class GetAuthorizedActionsHandler implements CommandHandler<GetAuthorizedActionsRequest, GetAuthorizedActionsResponse> {
    private final static Logger logger = LoggerFactory.getLogger(GetAuthorizedActionsHandler.class);

    private final AccessManager accessManager;
    private final TokenValidator tokenValidator;

    private final RoleOracle roleOracle;

    public GetAuthorizedActionsHandler(AccessManager accessManager, TokenValidator tokenValidator, RoleOracle roleOracle) {
        this.accessManager = accessManager;
        this.tokenValidator = tokenValidator;
        this.roleOracle = roleOracle;
    }

    @Nonnull
    @Override
    public String getChannelName() {
        return GetAuthorizedActionsRequest.CHANNEL;
    }

    @Override
    public Class<GetAuthorizedActionsRequest> getRequestClass() {
        return GetAuthorizedActionsRequest.class;
    }

    @Override
    public Mono<GetAuthorizedActionsResponse> handleRequest(GetAuthorizedActionsRequest request, ExecutionContext executionContext) {
        Set<ActionId> actions = new HashSet<>();

        try {
            //extract any SUPER admin actions from token
            List<RoleId> roleIds = tokenValidator.getTokenClaims(executionContext.jwt()).stream()
                    .map(RoleId::new)
                    .toList();
            actions.addAll(new HashSet<>(roleOracle.getActionsAssociatedToRoles(roleIds)));
        } catch (VerificationException e) {
            throw new RuntimeException(e);
        }

        actions.addAll(accessManager.getActionClosure(request.subject(),
                request.resource()));

        return Mono.just(new GetAuthorizedActionsResponse(request.resource(),
                request.subject(),
                actions));

    }
}
