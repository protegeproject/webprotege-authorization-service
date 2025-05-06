package edu.stanford.protege.webprotege.authorization;

import edu.stanford.protege.webprotege.ipc.CommandHandler;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import edu.stanford.protege.webprotege.ipc.WebProtegeHandler;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@WebProtegeHandler
public class GetProjectRoleDefinitionsHandler implements CommandHandler<GetProjectRoleDefinitionsRequest, GetProjectRoleDefinitionsResponse> {

    private final ProjectRoleDefinitionsManager projectRoleDefinitionsManager;

    public GetProjectRoleDefinitionsHandler(ProjectRoleDefinitionsManager projectRoleDefinitionsManager) {
        this.projectRoleDefinitionsManager = projectRoleDefinitionsManager;
    }

    @Nonnull
    @Override
    public String getChannelName() {
        return GetProjectRoleDefinitionsRequest.CHANNEL;
    }

    @Override
    public Class<GetProjectRoleDefinitionsRequest> getRequestClass() {
        return GetProjectRoleDefinitionsRequest.class;
    }

    @Override
    public Mono<GetProjectRoleDefinitionsResponse> handleRequest(GetProjectRoleDefinitionsRequest request, ExecutionContext executionContext) {
        var roleDefinitions = projectRoleDefinitionsManager.getEffectiveProjectRoleDefinitions(request.projectId());
        return Mono.just(new GetProjectRoleDefinitionsResponse(request.projectId(), roleDefinitions));
    }
}
