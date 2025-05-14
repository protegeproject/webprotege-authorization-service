package edu.stanford.protege.webprotege.authorization;

import edu.stanford.protege.webprotege.ipc.AuthorizedCommandHandler;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import edu.stanford.protege.webprotege.ipc.WebProtegeHandler;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@WebProtegeHandler
public class SetProjectRoleAssignmentsHandler implements AuthorizedCommandHandler<SetProjectRoleAssignmentsRequest, SetProjectRoleAssignmentsResponse> {

    private final AccessManager accessManager;

    public SetProjectRoleAssignmentsHandler(AccessManager accessManager) {
        this.accessManager = accessManager;
    }

    @Nonnull
    @Override
    public Resource getTargetResource(SetProjectRoleAssignmentsRequest request) {
        return ProjectResource.forProject(request.projectId());
    }

    @Nonnull
    @Override
    public Collection<Capability> getRequiredCapabilities() {
        return List.of(BuiltInCapability.EDIT_SHARING_SETTINGS.getCapability());
    }

    @Nonnull
    @Override
    public String getChannelName() {
        return SetProjectRoleAssignmentsRequest.CHANNEL;
    }

    @Override
    public Class<SetProjectRoleAssignmentsRequest> getRequestClass() {
        return SetProjectRoleAssignmentsRequest.class;
    }

    @Override
    public Mono<SetProjectRoleAssignmentsResponse> handleRequest(SetProjectRoleAssignmentsRequest request, ExecutionContext executionContext) {
        // Remove existing assignments
        var projectResource = ProjectResource.forProject(request.projectId());
        accessManager.getSubjectsWithAccessToResource(projectResource)
                .forEach(subject -> accessManager.setAssignedRoles(subject, projectResource, Collections.emptySet()));


        var projectAssignments = request.assignments();
        var byUserId = projectAssignments.userAssignments()
                .stream()
                .collect(Collectors.groupingBy(UserRoleAssignment::userId));
        byUserId.forEach((userId, assignments) -> {
            var roleIds = assignments.stream().map(UserRoleAssignment::roleId).collect(Collectors.toSet());
            accessManager.setAssignedRoles(
                    Subject.forUser(userId),
                    projectResource,
                    roleIds);

        });
        return Mono.just(new SetProjectRoleAssignmentsResponse(request.assignments()));
    }
}
