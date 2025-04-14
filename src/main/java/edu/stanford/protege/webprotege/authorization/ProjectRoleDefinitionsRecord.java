package edu.stanford.protege.webprotege.authorization;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.stanford.protege.webprotege.common.ProjectId;
import javax.annotation.Nonnull;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public record ProjectRoleDefinitionsRecord(@JsonProperty("_id") ProjectId projectId,
                                           @JsonProperty("roleDefinitions") Collection<RoleDefinition> roleDefinitions) {

    /**
     * Construct a {@link ProjectRoleDefinitionsRecord} without any validation.
     * @param projectId The project id which the definitons pertain to.
     * @param roleDefinitions The role definitions.
     */
    public ProjectRoleDefinitionsRecord(@JsonProperty("_id") ProjectId projectId, @JsonProperty("roleDefinitions") Collection<RoleDefinition> roleDefinitions) {
        this.projectId = Objects.requireNonNull(projectId);
        this.roleDefinitions = List.copyOf(Objects.requireNonNull(roleDefinitions));
    }

    /**
     * Construct a {@link ProjectRoleDefinitionsRecord} with validation that all of the specified roles are in fact
     * project roles.
     * @param projectId The project id that identifies the project to which the role definitions pertain.
     * @param roleDefinitions The role definitions.  These must all be of the type {@link RoleType#PROJECT_ROLE}
     *                        otherwise an {@link IllegalArgumentException} will be thrown.
     * @return The record.
     */
    @Nonnull
    public static ProjectRoleDefinitionsRecord get(@Nonnull ProjectId projectId,
                                                   @Nonnull Collection<RoleDefinition> roleDefinitions) {
        for(var roleDefinition : roleDefinitions) {
            if(!roleDefinition.roleType().equals(RoleType.PROJECT_ROLE)) {
                throw new IllegalArgumentException("Role is not a project role: " + roleDefinition);
            }
        }
        return new ProjectRoleDefinitionsRecord(projectId, roleDefinitions);
    }
}
