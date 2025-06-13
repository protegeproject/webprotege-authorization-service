package edu.stanford.protege.webprotege.authorization;

import edu.stanford.protege.webprotege.ipc.CommandHandler;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import edu.stanford.protege.webprotege.ipc.WebProtegeHandler;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2021-07-30
 */
@WebProtegeHandler
public class SetAssignedRolesHandler implements CommandHandler<SetAssignedRolesRequest, SetAssignedRolesResponse> {

    private final AccessManager accessManager;

    public SetAssignedRolesHandler(AccessManager accessManager) {
        this.accessManager = accessManager;
    }

    @Nonnull
    @Override
    public String getChannelName() {
        return SetAssignedRolesRequest.CHANNEL;
    }

    @Override
    public Class<SetAssignedRolesRequest> getRequestClass() {
        return SetAssignedRolesRequest.class;
    }

    @Override
    public Mono<SetAssignedRolesResponse> handleRequest(SetAssignedRolesRequest request, ExecutionContext executionContext) {
        accessManager.setAssignedRoles(request.subject(),
                                       request.resource(),
                                       request.roles());
        return Mono.just(new SetAssignedRolesResponse());
    }
}
