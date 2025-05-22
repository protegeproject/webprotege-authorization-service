package edu.stanford.protege.webprotege.authorization;

import com.google.common.collect.ImmutableSet;
import edu.stanford.protege.webprotege.authorization.handlers.*;
import edu.stanford.protege.webprotege.criteria.CompositeRootCriteria;
import edu.stanford.protege.webprotege.ipc.*;
import org.keycloak.common.VerificationException;
import org.slf4j.*;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2021-08-09
 */
@WebProtegeHandler
public class GetAuthorizedActionsForEntityHandler implements CommandHandler<GetAuthorizedCapabilitiesForEntityRequest, GetAuthorizedCapabilitiesForEntityResponse> {
    private final static Logger logger = LoggerFactory.getLogger(GetAuthorizedActionsForEntityHandler.class);

    private final AccessManager accessManager;
    private final TokenValidator tokenValidator;

    private final BuiltInRoleOracle builtInRoleOracle;

    private final CommandExecutor<GetMatchingCriteriaRequest, GetMatchingCriteriaResponse> getMatchingCriteriaExecutor;

    public GetAuthorizedActionsForEntityHandler(AccessManager accessManager,
                                                TokenValidator tokenValidator,
                                                BuiltInRoleOracle builtInRoleOracle,
                                                CommandExecutor<GetMatchingCriteriaRequest, GetMatchingCriteriaResponse> getMatchingCriteriaExecutor) {
        this.accessManager = accessManager;
        this.tokenValidator = tokenValidator;
        this.builtInRoleOracle = builtInRoleOracle;
        this.getMatchingCriteriaExecutor = getMatchingCriteriaExecutor;
    }

    @Nonnull
    @Override
    public String getChannelName() {
        return GetAuthorizedCapabilitiesForEntityRequest.CHANNEL;
    }

    @Override
    public Class<GetAuthorizedCapabilitiesForEntityRequest> getRequestClass() {
        return GetAuthorizedCapabilitiesForEntityRequest.class;
    }

    @Override
    public Mono<GetAuthorizedCapabilitiesForEntityResponse> handleRequest(GetAuthorizedCapabilitiesForEntityRequest request, ExecutionContext executionContext) {
        var capabilities = new HashSet<Capability>();

        var subject = Subject.forUser(request.userId());
        var resource = ProjectResource.forProject(request.projectId());

        try {
            var roleIds = tokenValidator.getTokenClaims(executionContext.jwt()).stream()
                    .map(RoleId::new)
                    .toList();
            capabilities.addAll(new HashSet<>(builtInRoleOracle.getCapabilitiesAssociatedToRoles(roleIds)));
        } catch (VerificationException e) {
            throw new RuntimeException(e);
        }

        capabilities.addAll(accessManager.getCapabilityClosure(subject, resource));

        Map<String, List<CompositeRootCriteria>> criteriaMap = extractUserPermisionCriteria(capabilities);

        return Mono.fromFuture(getMatchingCriteriaExecutor.execute(
                        new GetMatchingCriteriaRequest(criteriaMap,
                                resource.getProjectId().get(),
                                request.entityIri()),
                        executionContext))
                .map(matchResp -> {
                    var filteredCapabilities = capabilities.stream()
                            .filter(capability -> {
                                if (capability instanceof ContextAwareCapability cap) {
                                    return matchResp.matchingKeys().contains(cap.id());
                                }
                                return true;
                            })
                            .collect(Collectors.toSet());
                    return new GetAuthorizedCapabilitiesForEntityResponse(
                            ImmutableSet.copyOf(filteredCapabilities)
                    );
                });
    }

    private Map<String, List<CompositeRootCriteria>> extractUserPermisionCriteria(Set<Capability> capabilities) {
        Map<String, List<CompositeRootCriteria>> criteriaMap = new HashMap<>();

        for (Capability capability : capabilities) {
            if (capability instanceof ContextAwareCapability cap) {
                List<CompositeRootCriteria> existingCriteria = criteriaMap.get(cap.id());
                if (existingCriteria == null) {
                    existingCriteria = new ArrayList<>();
                }
                if (cap.contextCriteria() != null) {
                    existingCriteria.add((cap.contextCriteria()));
                }
                criteriaMap.put(cap.id(), existingCriteria);
            }
        }
        return criteriaMap;
    }
}
