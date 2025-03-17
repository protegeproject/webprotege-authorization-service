package edu.stanford.protege.webprotege.authorization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import edu.stanford.protege.webprotege.common.ProjectId;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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

    private static final Logger logger = LoggerFactory.getLogger(AccessManagerImpl.class);

    public static final String COLLECTION_NAME = "RoleAssignments";

    private final ObjectMapper objectMapper;

    private final RoleOracle roleOracle;

    private final MongoTemplate mongoTemplate;

    /**
     * Constructs an {@link AccessManager} that is backed by MongoDb.
     *
     * @param roleOracle    An oracle for looking up information about roles.
     * @param mongoTemplate A {@link MongoTemplate} that is used to access MongoDb.
     */
    public AccessManagerImpl(ObjectMapper objectMapper, RoleOracle roleOracle,
                             MongoTemplate mongoTemplate) {
        this.objectMapper = objectMapper;
        this.roleOracle = roleOracle;
        this.mongoTemplate = mongoTemplate;
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

    @Override
    public void setAssignedRoles(@Nonnull Subject subject,
                                 @Nonnull Resource resource,
                                 @Nonnull Collection<RoleId> roleIds) {
        var userName = toUserName(subject);
        var projectId = toProjectId(resource);
        var assignedRoles = roleIds.stream().map(RoleId::id).collect(toList());
        var roleClosure = getRoleClosure(roleIds);
        var capabilityClosure = getCapabilityClosure(roleIds);
        var assignment = new RoleAssignment(userName,
                projectId,
                assignedRoles,
                roleClosure,
                capabilityClosure);
        mongoTemplate.remove(withUserAndTarget(subject, resource), RoleAssignment.class);
        var doc = objectMapper.convertValue(assignment, Document.class);
        mongoTemplate.getCollection(COLLECTION_NAME).insertOne(doc);
    }

    private List<Capability> getCapabilityClosure(@Nonnull Collection<RoleId> roleIds) {
        return roleIds.stream()
                .flatMap(id -> roleOracle.getRoleClosure(id).stream())
                .flatMap(r -> r.capabilities().stream())
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
        var stream = find(query);
        return stream
                .map(f -> objectMapper.convertValue(f, RoleAssignment.class))
                .flatMap(ra -> ra.getAssignedRoles().stream())
                .map(RoleId::new)
                .distinct()
                .collect(toList());
    }

    private Stream<Document> find(Query query) {
        var found = mongoTemplate.getCollection(COLLECTION_NAME)
                .find(query.getQueryObject());
        var stream = StreamSupport.stream(found.spliterator(), false);
        return stream;
    }

    private Query withUserOrAnyUserAndTarget(Subject subject, Resource resource) {
        var userName = toUserName(subject);
        var projectId = toProjectId(resource);

        var query = query(where(PROJECT_ID).is(projectId));
        if (!subject.isGuest()) {
            query.addCriteria(where(USER_NAME).in(userName, null));
        } else {
            query.addCriteria(where(USER_NAME).is(userName));
        }
        return query;
    }

    @Nonnull
    @Override
    public Collection<RoleId> getRoleClosure(@Nonnull Subject subject, @Nonnull Resource resource) {
        var query = withUserOrAnyUserAndTarget(subject, resource);
        return find(query)
                .map(f -> objectMapper.convertValue(f, RoleAssignment.class))
                .flatMap(ra -> ra.getRoleClosure().stream())
                .distinct()
                .map(RoleId::new)
                .collect(toList());
    }

    @Nonnull
    @Override
    public Set<Capability> getCapabilityClosure(@Nonnull Subject subject, @Nonnull Resource resource) {
        var query = withUserOrAnyUserAndTarget(subject, resource);
        return find(query)
                .map(f -> objectMapper.convertValue(f, RoleAssignment.class))
                .flatMap(ra -> ra.getCapabilityClosure().stream())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public boolean hasPermission(@Nonnull Subject subject, @Nonnull Resource resource, @Nonnull Capability capability) {
        logger.info("Checking permission for subject {} and resource {} with capability: {}", subject, resource, capability);

        boolean match = find(withUserOrAnyUserAndTarget(subject, resource))
                .map(d -> objectMapper.convertValue(d, RoleAssignment.class))
                .anyMatch(roleAssignment -> roleAssignment.getCapabilityClosure().contains(capability));

//        var query = withUserOrAnyUserAndTarget(subject, resource)
//                .addCriteria(where(ACTION_CLOSURE).is(capability.id()))
//                .limit(1);
//        return mongoTemplate.count(query, RoleAssignment.class) == 1;
        return match;
    }

    @Override
    public Collection<Subject> getSubjectsWithAccessToResource(Resource resource) {
        return getSubjectsWithAccessToResource(resource, Optional.empty());
    }

    @Override
    public Collection<Subject> getSubjectsWithAccessToResource(Resource resource, Capability capabilityId) {
        return getSubjectsWithAccessToResource(resource, Optional.of(capabilityId));
    }

    private Collection<Subject> getSubjectsWithAccessToResource(Resource resource, Optional<Capability> capability) {
        String projectId = toProjectId(resource);
        Query query = query(where(PROJECT_ID).is(projectId));
        capability.ifPresent(a -> query.addCriteria(where(ACTION_CLOSURE).in(a.id())));
        return find(query)
                .map(f -> objectMapper.convertValue(f, RoleAssignment.class))
                .map(ra -> {
                    Optional<String> userName = ra.getUserName();
                    return userName.map(Subject::forUser).orElseGet(Subject::forAnySignedInUser);
                })
                .collect(toList());
    }

    @Override
    public Collection<Resource> getResourcesAccessibleToSubject(Subject subject, Capability capability) {
        var userName = toUserName(subject);
        var query = query(where(USER_NAME).is(userName).and(ACTION_CLOSURE).is(capability.id()));
        return find(query)
                .map(f -> objectMapper.convertValue(f, RoleAssignment.class))
                .map(ra -> {
                    Optional<String> projectId = ra.getProjectId();
                    if (projectId.isPresent()) {
                        return new ProjectResource(new ProjectId(projectId.get()));
                    } else {
                        return ApplicationResource.get();
                    }
                })
                .collect(toList());
    }

    @Override
    public void rebuild() {
        var collection = mongoTemplate.getCollection(COLLECTION_NAME);
        collection.find(new Query().getQueryObject())
                .map(f -> objectMapper.convertValue(f, RoleAssignment.class))
                .forEach(roleAssignment -> {
                    List<RoleId> assignedRoles = roleAssignment.getAssignedRoles().stream()
                            .map(RoleId::new)
                            .collect(Collectors.toList());
                    List<String> roleClosure = getRoleClosure(assignedRoles);
                    List<Capability> capabilityClosure = getCapabilityClosure(assignedRoles);
                    List<Document> capabilityClosureDocs = capabilityClosure.stream()
                            .map(c -> objectMapper.convertValue(c, Document.class))
                            .toList();
                    var query = query(where(USER_NAME).is(roleAssignment.getUserName().orElse(null))
                            .and(PROJECT_ID).is(roleAssignment.getProjectId().orElse(null)))
                            .getQueryObject();
                    collection.updateMany(query, new Update().set(ACTION_CLOSURE, capabilityClosureDocs)
                            .set(ROLE_CLOSURE, roleClosure).getUpdateObject());
                });

    }

}
