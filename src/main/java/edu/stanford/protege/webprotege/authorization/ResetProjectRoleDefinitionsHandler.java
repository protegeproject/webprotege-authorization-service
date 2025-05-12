package edu.stanford.protege.webprotege.authorization;

import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.ipc.AuthorizedCommandHandler;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import edu.stanford.protege.webprotege.ipc.WebProtegeHandler;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

@WebProtegeHandler
public class ResetProjectRoleDefinitionsHandler implements AuthorizedCommandHandler<ResetProjectRoleDefinitionsRequest, ResetProjectRoleDefinitionsResponse> {

    private final ProjectRoleDefinitionsManager roleDefinitionsManager;

    public ResetProjectRoleDefinitionsHandler(ProjectRoleDefinitionsManager roleDefinitionsManager) {
        this.roleDefinitionsManager = roleDefinitionsManager;
    }

    @Nonnull
    @Override
    public String getChannelName() {
        return ResetProjectRoleDefinitionsRequest.CHANNEL;
    }

    @Nonnull
    @Override
    public Resource getTargetResource(ResetProjectRoleDefinitionsRequest request) {
        return ProjectResource.forProject(request.projectId());
    }

    @Nonnull
    @Override
    public Collection<Capability> getRequiredCapabilities() {
        return List.of(BuiltInCapability.EDIT_PROJECT_SETTINGS.getCapability());
    }

    @Override
    public Class<ResetProjectRoleDefinitionsRequest> getRequestClass() {
        return ResetProjectRoleDefinitionsRequest.class;
    }

    @Override
    public Mono<ResetProjectRoleDefinitionsResponse> handleRequest(ResetProjectRoleDefinitionsRequest request, ExecutionContext executionContext) {
        var projectId = request.projectId();
        roleDefinitionsManager.clearProjectRoleDefinitions(projectId);
        var roleDefinitions = roleDefinitionsManager.getEffectiveProjectRoleDefinitions(projectId);
        return Mono.just(new ResetProjectRoleDefinitionsResponse(roleDefinitions));
    }
}
