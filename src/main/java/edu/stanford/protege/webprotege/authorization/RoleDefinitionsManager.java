package edu.stanford.protege.webprotege.authorization;

import edu.stanford.protege.webprotege.common.ProjectId;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Collection;

@Component
public class RoleDefinitionsManager {

    private final ProjectRoleDefinitionsManager projectRoleDefinitionsManager;

    private final BuiltInRoleOracle builtInRoleOracle;


    public RoleDefinitionsManager(ProjectRoleDefinitionsManager projectRoleDefinitionsManager, BuiltInRoleOracle builtInRoleOracle) {
        this.projectRoleDefinitionsManager = projectRoleDefinitionsManager;
        this.builtInRoleOracle = builtInRoleOracle;
    }

    /**
     * Gets the role definition closure for the specified role.  If the role is a built-in application role then
     * the built-in role definition closure for the role will be returned.  If the role is a project role then the role closure
     * is based on whether the project roles have been customized.  If the project roles have been customized then
     * the customized role definition closure will be returned.  If the project roles have not been customized then
     * the built-in project role definition closure will be returned.
     *
     * @param roleId The role id for which the role definition closure will be returned.
     * @param projectId The project id to check for project role definition customization.  The
     *                  project id is only taken into consideration if the specified role id is NOT a built-in
     *                  application role.  The project id may be null.  If this is the case then the built-in
     *                  project role definition closure will be returned if the role id is a project role.
     */
    public Collection<RoleDefinition> getRoleDefinitionClosure(RoleId roleId, @Nullable ProjectId projectId) {
        if(BuiltInRole.isBuiltInApplicationRole(roleId)) {
            return builtInRoleOracle.getRoleClosure(roleId);
        }
        else {
            return projectRoleDefinitionsManager.getProjectRoleClosure(projectId, roleId);
        }
    }
}
