package edu.stanford.protege.webprotege.authorization;

import edu.stanford.protege.webprotege.common.ProjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static edu.stanford.protege.webprotege.authorization.RoleAssignment.*;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 7 Jan 2017
 */
@Component
public class AccessManagerImpl implements AccessManager {

    private final RoleOracle roleOracle;

    private final MongoTemplate mongoTemplate;

    /**
     * Constructs an {@link AccessManager} that is backed by MongoDb.
     *
     * @param roleOracle An oracle for looking up information about roles.
     * @param mongoTemplate  A {@link MongoTemplate} that is used to access MongoDb.
     */
    public AccessManagerImpl(RoleOracle roleOracle,
                             MongoTemplate mongoTemplate) {
        this.roleOracle = roleOracle;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void setAssignedRoles(@Nonnull Subject subject,
                                 @Nonnull Resource resource,
                                 @Nonnull Collection<RoleId> roleIds) {
        var userName = toUserName(subject);
        var projectId = toProjectId(resource);
        var assignedRoles = roleIds.stream().map(RoleId::id).collect(toList());
        var roleClosure = getRoleClosure(roleIds);
        var actionClosure = getActionClosure(roleIds);
        RoleAssignment assignment = new RoleAssignment(userName,
                                                       projectId,
                                                       assignedRoles,
                                                       roleClosure,
                                                       actionClosure);
        mongoTemplate.remove(withUserAndTarget(subject, resource), RoleAssignment.class);
        mongoTemplate.save(assignment);
    }

    private List<String> getActionClosure(@Nonnull Collection<RoleId> roleIds) {
        return roleIds.stream()
                      .flatMap(id -> roleOracle.getRoleClosure(id).stream())
                      .flatMap(r -> r.actions().stream())
                      .map(ActionId::id)
                      .sorted()
                      .collect(toList());
    }

    private List<String> getRoleClosure(@Nonnull Collection<RoleId> roleIds) {
        return roleIds.stream()
                      .flatMap(id -> roleOracle.getRoleClosure(id).stream())
                      .map(r -> r.roleId().id())
                      .collect(toList());
    }

    private Query withUserAndTarget(Subject subject, Resource resource) {
        var userName = toUserName(subject);
        var projectId = toProjectId(resource);

        return query(where(USER_NAME).is(userName))
                .addCriteria(where(PROJECT_ID).is(projectId));
    }

    @Nonnull
    @Override
    public Collection<RoleId> getAssignedRoles(@Nonnull Subject subject, @Nonnull Resource resource) {
        var query = withUserAndTarget(subject, resource);
        var found = mongoTemplate.find(query, RoleAssignment.class);
        return found.stream().flatMap(ra -> ra.getAssignedRoles().stream())
                    .map(RoleId::new)
                    .distinct()
                    .collect(toList());
    }

    private Query withUserOrAnyUserAndTarget(Subject subject, Resource resource) {
        var userName = toUserName(subject);
        var projectId = toProjectId(resource);

        var query = query(where(PROJECT_ID).is(projectId));
        if (!subject.isGuest()) {
            query.addCriteria(where(USER_NAME).in(userName, null));
        }
        else {
            query.addCriteria(where(USER_NAME).is(userName));
        }
        return query;
    }

    @Nonnull
    @Override
    public Collection<RoleId> getRoleClosure(@Nonnull Subject subject, @Nonnull Resource resource) {
        var query = withUserOrAnyUserAndTarget(subject, resource);
        var found = mongoTemplate.find(query, RoleAssignment.class);
        return found.stream()
                    .flatMap(ra -> ra.getRoleClosure().stream())
                    .distinct()
                    .map(RoleId::new)
                    .collect(toList());
    }

    @Nonnull
    @Override
    public Set<ActionId> getActionClosure(@Nonnull Subject subject, @Nonnull Resource resource) {
        var query = withUserOrAnyUserAndTarget(subject, resource);
        return mongoTemplate.find(query, RoleAssignment.class)
                            .stream()
                            .flatMap(ra -> ra.getActionClosure().stream())
                            .map(ActionId::new)
                            .collect(toSet());
    }

    @Override
    public boolean hasPermission(@Nonnull Subject subject, @Nonnull Resource resource, @Nonnull ActionId actionId) {
        var query = withUserOrAnyUserAndTarget(subject, resource)
                .addCriteria(where(ACTION_CLOSURE).is(actionId.id()))
                .limit(1);
        return mongoTemplate.count(query, RoleAssignment.class) == 1;
    }

    @Override
    public boolean hasPermission(@Nonnull Subject subject,
                                 @Nonnull Resource resource,
                                 @Nonnull BuiltInAction builtInAction) {
        return hasPermission(subject, resource, builtInAction.getActionId());
    }

    @Override
    public Collection<Subject> getSubjectsWithAccessToResource(Resource resource) {
        return getSubjectsWithAccessToResource(resource, Optional.empty());
    }

    @Override
    public Collection<Subject> getSubjectsWithAccessToResource(Resource resource, ActionId actionId) {
        return getSubjectsWithAccessToResource(resource, Optional.of(actionId));
    }

    private Collection<Subject> getSubjectsWithAccessToResource(Resource resource, Optional<ActionId> action) {
        String projectId = toProjectId(resource);
        Query query = query(where(PROJECT_ID).is(projectId));
        action.ifPresent(a -> query.addCriteria(where(ACTION_CLOSURE).in(a.id())));
        return mongoTemplate.find(query, RoleAssignment.class)
                .stream()
                .map(ra -> {
                    Optional<String> userName = ra.getUserName();
                    if (userName.isPresent()) {
                        return Subject.forUser(userName.get());
                    }
                    else {
                        return Subject.forAnySignedInUser();
                    }
                })
                .collect(toList());
    }

    @Override
    public Collection<Resource> getResourcesAccessibleToSubject(Subject subject, ActionId actionId) {
        var userName = toUserName(subject);
        var query = query(where(USER_NAME).is(userName).and(ACTION_CLOSURE).is(actionId.id()));
        return mongoTemplate.find(query, RoleAssignment.class).stream()
                            .map(ra -> {
                        Optional<String> projectId = ra.getProjectId();
                        if (projectId.isPresent()) {
                            return new ProjectResource(new ProjectId(projectId.get()));
                        }
                        else {
                            return ApplicationResource.get();
                        }
                    })
                            .collect(toList());
    }

    @Override
    public void rebuild() {
        mongoTemplate.find(new Query(), RoleAssignment.class)
                     .forEach(roleAssignment -> {
                 List<RoleId> assignedRoles = roleAssignment.getAssignedRoles().stream()
                         .map(RoleId::new)
                         .collect(Collectors.toList());
                 List<String> roleClosure = getRoleClosure(assignedRoles);
                 List<String> actionClosure = getActionClosure(assignedRoles);
                 var query = query(where(USER_NAME).is(roleAssignment.getUserName().orElse(null))
                                                   .and(PROJECT_ID).is(roleAssignment.getProjectId().orElse(null)));
                 mongoTemplate.updateMulti(query, new Update().set(ACTION_CLOSURE, actionClosure).set(ROLE_CLOSURE, roleClosure), RoleAssignment.class);
             });

    }

    /**
     * Converts the specified subject to a user name or a null value if the specified subject does not
     * represent a user.
     *
     * @param subject The subject.
     * @return The user name for the subject.
     */
    @Nullable
    private static String toUserName(@Nonnull Subject subject) {
        return subject.getUserName().orElse(null);
    }

    @Nullable
    private static String toProjectId(Resource resource) {
        return resource.getProjectId().map(ProjectId::id).orElse(null);
    }

}
