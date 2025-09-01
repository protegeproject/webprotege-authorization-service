package edu.stanford.protege.webprotege.authorization;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.protege.webprotege.common.EventId;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.ipc.EventDispatcher;
import org.bson.Document;
import org.keycloak.common.VerificationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static edu.stanford.protege.webprotege.authorization.RoleAssignment.*;
import static java.util.stream.Collectors.toList;
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
    private static final String ROLE_ASSIGNMENTS_CACHE = "roleAssignments";

    private final ObjectMapper objectMapper;

    private final MongoTemplate mongoTemplate;

    private final ProjectRoleDefinitionsManager projectRoleDefinitionsManager;

    private final RoleDefinitionsManager roleDefinitionsManager;

    private final EventDispatcher eventDispatcher;

    private final TokenValidator tokenValidator;

    private final BuiltInRoleOracle builtInRoleOracle;

    /**
     * Constructs an {@link AccessManager} that is backed by MongoDb.
     *
     * @param mongoTemplate     A {@link MongoTemplate} that is used to access MongoDb.
     * @param tokenValidator
     * @param builtInRoleOracle
     */
    public AccessManagerImpl(ObjectMapper objectMapper,
                             MongoTemplate mongoTemplate,
                             ProjectRoleDefinitionsManager projectRoleDefinitionsManager,
                             RoleDefinitionsManager roleDefinitionsManager,
                             EventDispatcher eventDispatcher, TokenValidator tokenValidator, BuiltInRoleOracle builtInRoleOracle) {
        this.objectMapper = objectMapper;
        this.mongoTemplate = mongoTemplate;
        this.projectRoleDefinitionsManager = projectRoleDefinitionsManager;
        this.roleDefinitionsManager = roleDefinitionsManager;
        this.eventDispatcher = eventDispatcher;
        this.tokenValidator = tokenValidator;
        this.builtInRoleOracle = builtInRoleOracle;
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
    private static String toProjectIdString(Resource resource) {
        return resource.getProjectId().map(ProjectId::id).orElse(null);
    }

    @CacheEvict(value = ROLE_ASSIGNMENTS_CACHE, allEntries = true)
    @Override
    public void setAssignedRoles(@Nonnull Subject subject,
                                 @Nonnull Resource resource,
                                 @Nonnull Collection<RoleId> roleIds) {

        var userName = toUserName(subject);
        var projectId = resource.getProjectId();

        var roleDefinitionsClosure = new HashSet<RoleDefinition>();

        // For each assigned role, we get its closure
        for (var roleId : roleIds) {
            var defs = roleDefinitionsManager.getRoleDefinitionClosure(roleId, projectId.orElse(null));
            roleDefinitionsClosure.addAll(defs);
        }

        // Next we get the capabilities of the closure
        var roleCapabilitiesClosure = roleDefinitionsClosure.stream()
                .map(RoleDefinition::roleCapabilities)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        var assignedRoles = roleIds.stream().map(RoleId::id).toList();
        var roleIdClosure = roleDefinitionsClosure.stream().map(RoleDefinition::roleId).map(RoleId::id).toList();
        var assignment = new RoleAssignment(userName,
                projectId.map(ProjectId::id).orElse(null),
                assignedRoles,
                roleIdClosure,
                List.copyOf(roleCapabilitiesClosure));
        mongoTemplate.remove(withUserAndTarget(subject, resource), RoleAssignment.class);
        var doc = objectMapper.convertValue(assignment, Document.class);
        mongoTemplate.getCollection(COLLECTION_NAME).insertOne(doc);

        resource.getProjectId()
                .ifPresent(prj -> {
                    eventDispatcher.dispatchEvent(new PermissionsChangedEvent(EventId.generate(), prj));
                });
    }

    @Override
    public void setProjectRoleAssignments(ProjectId projectId, ProjectRoleAssignments projectRoleAssignments) {

        // Remove existing assignments
        var projectResource = ProjectResource.forProject(projectId);
        getSubjectsWithAccessToResource(projectResource)
                .forEach(subject -> setAssignedRoles(subject, projectResource, Collections.emptySet()));

        List<UserRoleAssignment> userRoleAssignments = projectRoleAssignments.userAssignments();
        var byUserId = userRoleAssignments
                .stream()
                .collect(Collectors.groupingBy(UserRoleAssignment::userId));
        byUserId.forEach((userId, assignments) -> {
            var roleIds = assignments.stream().map(UserRoleAssignment::roleId).collect(Collectors.toSet());
            setAssignedRoles(
                    Subject.forUser(userId),
                    projectResource,
                    roleIds);

        });
    }

    private List<Capability> getCapabilityClosure(@Nullable ProjectId projectId,
                                                  @Nonnull Collection<RoleId> roleIds) {

        return roleIds.stream()
                .flatMap(id -> projectRoleDefinitionsManager.getProjectRoleClosure(projectId, id).stream())
                .flatMap(r -> r.roleCapabilities().stream())
                .collect(toList());

    }

    private Query withUserAndTarget(Subject subject, Resource resource) {
        var userName = toUserName(subject);
        var projectId = toProjectIdString(resource);
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
        var projectId = toProjectIdString(resource);

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
    public boolean hasPermission(@Nonnull Subject subject, @Nonnull Resource resource, @Nonnull Capability capability, String jwt) {
        logger.info("Checking permission for subject {} and resource {} with capability: {}", subject, resource, capability);


        List<Capability> capabilities = new ArrayList<>(find(withUserOrAnyUserAndTarget(subject, resource))
                .map(d -> objectMapper.convertValue(d, RoleAssignment.class))
                .flatMap(roleAssignment -> roleAssignment.getCapabilityClosure().stream())
                .toList());

        if (jwt != null && !jwt.isEmpty()) {
            try {
                List<RoleId> roleIds = tokenValidator.extractClaimsWithoutVerification(jwt).stream()
                        .map(RoleId::new)
                        .toList();
                capabilities.addAll(builtInRoleOracle.getCapabilitiesAssociatedToRoles(roleIds));
            } catch (VerificationException e) {
                logger.error("Error getting token claims", e);
                throw new RuntimeException(e);
            }
        }


        return capabilities.contains(capability);


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

        var projectId = toProjectIdString(resource);
        var query = query(where(PROJECT_ID).is(projectId));
        capability.ifPresent(a -> query.addCriteria(where(CAPABILITY_CLOSURE + ".id").in(a.id())));
        return find(query)
                .map(f -> objectMapper.convertValue(f, RoleAssignment.class))
                .filter(ra -> capability.map(cap -> ra.getCapabilityClosure().contains(cap)).orElse(true))
                .map(ra -> {
                    var userName = ra.getUserName();
                    return userName.map(Subject::forUser).orElseGet(Subject::forAnySignedInUser);
                })
                .collect(toList());

    }

    @Override
    public Collection<Resource> getResourcesAccessibleToSubject(Subject subject, Capability capability) {

        var userName = toUserName(subject);
        logger.info("Trying to fetch resources {} and capability {}", userName, capability.id());
        var query = query(where(USER_NAME).is(userName).and(CAPABILITY_CLOSURE + ".id").is(capability.id()));
        return find(query)
                .map(f -> objectMapper.convertValue(f, RoleAssignment.class))
                .filter(ra -> ra.getCapabilityClosure().contains(capability))
                .map(ra -> {
                    var projectId = ra.getProjectId();
                    if (projectId.isPresent()) {
                        return new ProjectResource(new ProjectId(projectId.get()));
                    } else {
                        return ApplicationResource.get();
                    }
                })
                .collect(toList());

    }

    @Cacheable(value = ROLE_ASSIGNMENTS_CACHE, key = "#projectId.value()", unless = "#result.isEmpty()")
    @Override
    public List<RoleAssignment> getRoleAssignments(ProjectId projectId) {

        var query = query(where(PROJECT_ID).is(projectId.value()));
        return find(query)
                .map(f -> objectMapper.convertValue(f, RoleAssignment.class))
                .toList();

    }

    @CacheEvict(value = ROLE_ASSIGNMENTS_CACHE, allEntries = true)
    @Override
    public void rebuild() {

        logger.info("Rebuilding permissions");
        var queryObject = new Query().getQueryObject();
        rebuildMatchingRoleAssignments(queryObject);

    }

    @CacheEvict(value = ROLE_ASSIGNMENTS_CACHE, key = "#projectId.value()")
    @Override
    public void rebuild(ProjectId projectId) {

        logger.info("Rebuilding permissions for project: {}", projectId);
        var criteria = where(PROJECT_ID).is(projectId.value());
        var queryObject = new Query(criteria).getQueryObject();
        rebuildMatchingRoleAssignments(queryObject);

        eventDispatcher.dispatchEvent(new PermissionsChangedEvent(EventId.generate(), projectId));
    }

    private void rebuildMatchingRoleAssignments(Document queryObject) {
        var collection = mongoTemplate.getCollection(COLLECTION_NAME);
        collection.find(queryObject)
                .forEach(roleAssignmentDoc -> {
                    final var roleAssignmentId = roleAssignmentDoc.get("_id");
                    var roleAssignment = objectMapper.convertValue(roleAssignmentDoc, RoleAssignment.class);

                    // For each role assignment we compute the role closure and then
                    // compute the capability closure
                    var projectId = roleAssignment.getProjectId().map(ProjectId::valueOf).orElse(null);
                    var assignedRoles = roleAssignment.getAssignedRoles().stream()
                            .map(RoleId::new)
                            .collect(Collectors.toList());

                    var capabilityClosure = new LinkedHashSet<Capability>();
                    var roleClosure = new LinkedHashSet<RoleDefinition>();
                    assignedRoles.forEach(assignedRole -> {
                        var assignedRoleClosure = projectRoleDefinitionsManager.getProjectRoleClosure(projectId, assignedRole);
                        roleClosure.addAll(assignedRoleClosure);
                        var roleCapabilitiesClosure = getCapabilityClosure(projectId, assignedRoles);
                        capabilityClosure.addAll(roleCapabilitiesClosure);
                    });

                    var sortedCapabilityClosure = capabilityClosure.stream()
                            .sorted(Comparator.comparing(Capability::id)).toList();
                    var updatedRoleAssignment = new RoleAssignment(roleAssignment.getUserName().orElse(null),
                            roleAssignment.getProjectId().orElse(null),
                            roleAssignment.getAssignedRoles(),
                            roleClosure.stream().map(RoleDefinition::roleId).map(RoleId::id).toList(),
                            sortedCapabilityClosure);

                    var updatedRoleAssignmentDoc = objectMapper.convertValue(updatedRoleAssignment, Document.class);
                    updatedRoleAssignmentDoc.put("_id", roleAssignmentId);
                    collection.replaceOne(new Document("_id", roleAssignmentId), updatedRoleAssignmentDoc);
                });
    }

}
