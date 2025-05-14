package edu.stanford.protege.webprotege.authorization;

import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.common.UserId;
import edu.stanford.protege.webprotege.ipc.CommandHandler;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import edu.stanford.protege.webprotege.ipc.WebProtegeHandler;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

@WebProtegeHandler
public class GetProjectRoleAssignmentsHandler implements CommandHandler<GetProjectRoleAssignmentsRequest, GetProjectRoleAssignmentsResponse> {

    private final AccessManager accessManager;

    public GetProjectRoleAssignmentsHandler(AccessManager accessManager) {
        this.accessManager = accessManager;
    }

    @Nonnull
    @Override
    public String getChannelName() {
        return GetProjectRoleAssignmentsRequest.CHANNEL;
    }

    @Override
    public Class<GetProjectRoleAssignmentsRequest> getRequestClass() {
        return GetProjectRoleAssignmentsRequest.class;
    }

    @Override
    public Mono<GetProjectRoleAssignmentsResponse> handleRequest(GetProjectRoleAssignmentsRequest request, ExecutionContext executionContext) {
        var roleAssignments = accessManager.getRoleAssignments(request.projectId());
        var userRoleAssignments = roleAssignments.stream()
                .filter(ra -> ra.getUserName().isPresent())
                .flatMap(ra -> {
                    var userId = UserId.valueOf(ra.getUserName().get());
                    var assignedRoles = ra.getAssignedRoles();
                    return assignedRoles.stream()
                            .map(RoleId::new)
                            .map(r -> new UserRoleAssignment(userId, r));
                })
                .toList();
        var assignments = new ProjectRoleAssignments(userRoleAssignments);
        return Mono.just(new GetProjectRoleAssignmentsResponse(assignments));
    }
}
