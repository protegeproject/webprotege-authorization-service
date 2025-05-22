package edu.stanford.protege.webprotege.authorization;

import edu.stanford.protege.webprotege.common.ProjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ProjectRoleDefinitionsManager {

    private static final Logger logger = LoggerFactory.getLogger(ProjectRoleDefinitionsManager.class);

    private final ProjectRoleDefinitionsRepository repository;

    public ProjectRoleDefinitionsManager(ProjectRoleDefinitionsRepository repository) {
        this.repository = repository;
    }

    /**
     * Sets a custom collection of role definitions for the specified project.  These role definitions must be project
     * role definitions.
     * @param projectId The project id that identifies the project to which the specified roles will pertain.
     * @param roleDefinitions The role definitions.  This should be a complete collection of project roles.
     */
    public void setProjectRoleDefinitions(@Nonnull ProjectId projectId,
                                          @Nonnull Collection<RoleDefinition> roleDefinitions) {
        for(var roleDefinition : roleDefinitions) {
            if(!roleDefinition.roleType().equals(RoleType.PROJECT_ROLE)) {
                throw new IllegalArgumentException("Role definition is not a project role: " + roleDefinition);
            }
        }
        var record = ProjectRoleDefinitionsRecord.get(projectId, roleDefinitions);
        repository.saveProjectRoleDefinitions(record);
    }

    /**
     * Clears the role definitions for the specified project.  This removes any customization of project roles
     * for the specified project.
     * @param projectId The project.
     */
    public void clearProjectRoleDefinitions(@Nonnull ProjectId projectId) {
        repository.clearProjectRoleDefinitions(projectId);
    }

    /**
     * Gets the role definitions that are in effect for the specified project.
     * Note that projects may wholesale replace the built-in project role definitions.
     * If the specified project does not replace any role definitions then the built-in project role definitions
     * will be returned.
     * @param projectId The project id.  If this is null then the application wide role definitions will be returned.
     * @return A list of role definitions that apply to the specified project.
     */
    public List<RoleDefinition> getEffectiveProjectRoleDefinitions(@Nullable ProjectId projectId) {
        if(projectId == null) {
            return getBuiltInProjectRoleDefinitions();
        }
        var record = repository.getProjectRoleDefinitions(projectId);
        if(record.isEmpty()) {
            return getBuiltInProjectRoleDefinitions();
        }
        else {
            return List.copyOf(record.get().roleDefinitions());
        }
    }


    /**
     * Gets the role closure for a given set of project role Ids in the context of a project.
     * The role closure includes the set of roles and parent roles (transitively) of each role in the set.
     * @param projectId The project ID. If null, uses application-wide role definitions.
     * @param roleIds The set of role IDs to compute the closure for.
     * @return A set of role definitions that includes the specified set of roles and all the ancestor roles.
     * @throws IllegalStateException if a cycle is detected in the role hierarchy.
     */
    public Set<RoleDefinition> getProjectRoleClosure(@Nullable ProjectId projectId, Collection<RoleId> roleIds) {
        return roleIds.stream()
                .map(roleId -> getProjectRoleClosure(projectId, roleId))
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
    }

    /**
     * Gets the role closure for a given project role ID in the context of a project.
     * The role closure includes the role itself and all its parent roles (transitively).
     * @param projectId The project ID. If null, uses application-wide role definitions.
     * @param roleId The role ID to compute the closure for.
     * @return A set of role definitions that includes the specified role and all its parent roles.
     * @throws IllegalStateException if a cycle is detected in the role hierarchy.
     */
    @Nonnull
    public Set<RoleDefinition> getProjectRoleClosure(@Nullable ProjectId projectId, @Nonnull RoleId roleId) {
        var projectRoleDefinitions = getEffectiveProjectRoleDefinitions(projectId);
        var roleDefinitionsMap = new HashMap<RoleId, RoleDefinition>();
        for(var roleDefinition : projectRoleDefinitions) {
            roleDefinitionsMap.put(roleDefinition.roleId(), roleDefinition);
        }

        var result = new HashSet<RoleDefinition>();
        var toProcess = new ArrayDeque<RoleId>();
        var processing = new HashSet<RoleId>();
        toProcess.add(roleId);

        while (!toProcess.isEmpty()) {
            var currentRoleId = toProcess.removeFirst();
            if (!processing.add(currentRoleId)) {
                // We've seen this role before while processing - we have a cycle.  This is allowed but it's probably
                // a mistake.  Log the cycle.
                logRoleHierarchyCycle(currentRoleId, toProcess, roleDefinitionsMap);
            }
            var currentRole = roleDefinitionsMap.get(currentRoleId);
            if (currentRole != null && result.add(currentRole)) {
                toProcess.addAll(currentRole.parentRoles());
            }
        }
        return result;
    }

    private static void logRoleHierarchyCycle(RoleId currentRoleId, ArrayDeque<RoleId> toProcess, HashMap<RoleId, RoleDefinition> roleDefinitionsMap) {
        var cyclePath = new ArrayList<RoleId>();
        cyclePath.add(currentRoleId);
        var current = currentRoleId;
        while (toProcess.contains(current)) {
            Iterator<RoleId> iter = roleDefinitionsMap.get(current).parentRoles().iterator();
            if(iter.hasNext()) {
                current = iter.next();
                cyclePath.add(current);
            }

        }
        logger.warn("Cycle detected in role hierarchy: {}", String.join(" -> ",
                cyclePath.stream().map(RoleId::id).toList()));
    }

    private static List<RoleDefinition> getBuiltInProjectRoleDefinitions() {
        return Arrays.stream(BuiltInRole.values())
                .filter(BuiltInRole::isProjectRole)
                .map(BuiltInRole::toRoleDefinition)
                .toList();
    }

}
