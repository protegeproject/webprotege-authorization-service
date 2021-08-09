package edu.stanford.protege.webprotege.authorization;

import edu.stanford.protege.webprotege.authorization.api.GetAssignedRolesRequest;
import edu.stanford.protege.webprotege.authorization.api.GetAssignedRolesResponse;
import edu.stanford.protege.webprotege.authorization.api.Resource;
import edu.stanford.protege.webprotege.authorization.api.Subject;
import edu.stanford.protege.webprotege.ipc.CommandHandler;
import edu.stanford.protege.webprotege.ipc.WebProtegeHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

import java.util.HashSet;
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
        return GetAssignedRolesRequest.CHANNEL_NAME;
    }

    @Override
    public Class<GetAssignedRolesRequest> getRequestClass() {
        return GetAssignedRolesRequest.class;
    }

    @Override
    public Mono<GetAssignedRolesResponse> handleRequest(GetAssignedRolesRequest request) {
        var subject = request.subject();
        var resource = request.resource();
        var assignedRoles = accessManager.getAssignedRoles(subject, resource);
        var response = new GetAssignedRolesResponse(subject, resource, Set.copyOf(assignedRoles));
        return Mono.just(response);
    }
}
