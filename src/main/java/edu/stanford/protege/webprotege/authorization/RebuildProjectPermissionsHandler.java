package edu.stanford.protege.webprotege.authorization;

import edu.stanford.protege.webprotege.ipc.AuthorizedCommandHandler;
import edu.stanford.protege.webprotege.ipc.CommandHandler;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import edu.stanford.protege.webprotege.ipc.WebProtegeHandler;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

@WebProtegeHandler
public class RebuildProjectPermissionsHandler implements CommandHandler<RebuildProjectPermissionsRequest, RebuildProjectPermissionsResponse> {

    private final AccessManager accessManager;

    public RebuildProjectPermissionsHandler(AccessManager accessManager) {
        this.accessManager = accessManager;
    }

    @Nonnull
    @Override
    public String getChannelName() {
        return RebuildProjectPermissionsRequest.CHANNEL;
    }

    @Override
    public Class<RebuildProjectPermissionsRequest> getRequestClass() {
        return RebuildProjectPermissionsRequest.class;
    }

    @Override
    public Mono<RebuildProjectPermissionsResponse> handleRequest(RebuildProjectPermissionsRequest request, ExecutionContext executionContext) {
        accessManager.rebuild(request.projectId());
        return Mono.just(new RebuildProjectPermissionsResponse());
    }
}
