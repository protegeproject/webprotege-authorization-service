package edu.stanford.protege.webprotege.authorization;


import edu.stanford.protege.webprotege.common.ProjectId;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 4 Jan 2017
 */
public interface AccessManager {

    /**
     * Get the role ids that have been assigned to the specified subject for the specified resource.
     * @param subject The subject.
     * @param resource The resoruce.
     * @return The assigned role ids for the subject and resource pair.
     */
    @Nonnull
    Collection<RoleId> getAssignedRoles(@Nonnull Subject subject,
                                        @Nonnull Resource resource);

    /**
     * Sets the assigned roles for the specified subject and resource pair.
     * @param subject The subject.
     * @param resource The resource.
     * @param roleIds The role ids to be assigned.  These will replace any existing assigned role ids for the
     *                specified subject and resource pair.
     */
    void setAssignedRoles(@Nonnull Subject subject,
                          @Nonnull Resource resource,
                          @Nonnull Collection<RoleId> roleIds);

    void setProjectRoleAssignments(ProjectId projectId, ProjectRoleAssignments projectRoleAssignments);

    /**
     * Gets the role closure for the specified subject and resource pair.
     * @param subject The subject.
     * @param resource The resource.
     * @return A collection of role ids that are in the role closure for the specified subject and resource pair.
     */
    @Nonnull
    Collection<RoleId> getRoleClosure(@Nonnull Subject subject,
                                      @Nonnull Resource resource);

    /**
     * Gets the capability closure for the specified subject and resource pair.
     * @param subject The subject.
     * @param resource The resource.
     * @return A collection of capabilities that belong to the role closure of the specified subject and resource pair.
     */
    @Nonnull
    Set<Capability> getCapabilityClosure(@Nonnull Subject subject,
                                       @Nonnull Resource resource);

    /**
     * Tests to see if the specified subject has permission to execute the specified capability on the specified resource.
     *
     * @param subject    The subject.
     * @param resource   The resource on which the capability should be executed.
     * @param capability The required capability.
     * @return {@code true} if the subject has permission to execute the specified capability on the specified resource,
     * otherwise {@code false}.
     */
    boolean hasPermission(@Nonnull Subject subject,
                          @Nonnull Resource resource,
                          @Nonnull Capability capability);

    Collection<Subject> getSubjectsWithAccessToResource(Resource resource);

    Collection<Subject> getSubjectsWithAccessToResource(Resource resource, Capability capability);

    Collection<Resource> getResourcesAccessibleToSubject(Subject subject, Capability capabilityId);


    /**
     * Rebuilds the role and capability closure for all subjects and resources.
     */
    void rebuild();

    void rebuild(ProjectId projectId);

    List<RoleAssignment> getRoleAssignments(ProjectId projectId);
}