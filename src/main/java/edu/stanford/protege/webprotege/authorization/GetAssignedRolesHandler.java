package edu.stanford.protege.webprotege.authorization;

import edu.stanford.protege.webprotege.ipc.CommandHandler;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import edu.stanford.protege.webprotege.ipc.WebProtegeHandler;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2021-08-09
 */
@WebProtegeHandler
public class GetAssignedRolesHandler implements CommandHandler<GetAssignedRolesRequest, GetAssignedRolesResponse> {

    private final AccessManager accessManager;

    public GetAssignedRolesHandler(AccessManager accessManager) {
        this.accessManager = requireNonNull(accessManager);
    }

    @Nonnull
    @Override
    public String getChannelName() {
        return GetAssignedRolesRequest.CHANNEL;
    }

    @Override
    public Class<GetAssignedRolesRequest> getRequestClass() {
        return GetAssignedRolesRequest.class;
    }

    @Override
    public Mono<GetAssignedRolesResponse> handleRequest(GetAssignedRolesRequest request, ExecutionContext executionContext) {
        var subject = request.subject();
        var resource = request.resource();
        var assignedRoles = accessManager.getAssignedRoles(subject, resource);
        var response = new GetAssignedRolesResponse(subject, resource, Set.copyOf(assignedRoles));
        return Mono.just(response);
    }
}
