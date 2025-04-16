package edu.stanford.protege.webprotege.authorization;

import edu.stanford.protege.webprotege.ipc.AuthorizedCommandHandler;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import edu.stanford.protege.webprotege.ipc.WebProtegeHandler;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

@WebProtegeHandler
public class SetProjectRoleDefinitionsHandler implements AuthorizedCommandHandler<SetProjectRoleDefinitionsRequest, SetProjectRoleDefinitionsResponse> {

    private final AccessManager accessManager;

    public SetProjectRoleDefinitionsHandler(AccessManager accessManager, ProjectRoleDefinitionsManager roleDefinitionsManager) {
        this.accessManager = accessManager;
        this.roleDefinitionsManager = roleDefinitionsManager;
    }

    private final ProjectRoleDefinitionsManager roleDefinitionsManager;

    @Nonnull
    @Override
    public Resource getTargetResource(SetProjectRoleDefinitionsRequest request) {
        return ProjectResource.forProject(request.projectId());
    }

    @Nonnull
    @Override
    public Collection<Capability> getRequiredCapabilities() {
        return List.<Capability>of(BuiltInCapability.EDIT_FORMS.getCapability());
    }

    @Nonnull
    @Override
    public String getChannelName() {
        return SetProjectRoleDefinitionsRequest.CHANNEL;
    }

    @Override
    public Class<SetProjectRoleDefinitionsRequest> getRequestClass() {
        return SetProjectRoleDefinitionsRequest.class;
    }

    @Override
    public Mono<SetProjectRoleDefinitionsResponse> handleRequest(SetProjectRoleDefinitionsRequest request, ExecutionContext executionContext) {
        roleDefinitionsManager.setProjectRoleDefinitions(request.projectId(),
                request.roleDefinitions());
        accessManager.rebuild(request.projectId());
        return Mono.just(SetProjectRoleDefinitionsResponse.get(request.roleDefinitions()));
    }
}
