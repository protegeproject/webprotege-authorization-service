package edu.stanford.protege.webprotege.authorization;

import edu.stanford.protege.webprotege.common.ProjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Guards against a regression of the crash logRoleHierarchyCycle used to cause: a role
 * whose parent chain is reached twice while resolving a closure (the code tolerates this
 * as a "cycle" - not necessarily a real one, a shared-ancestor diamond triggers the same
 * path) previously re-walked parentRoles() to build a log message, and that walk NPE'd
 * the instant it reached a role id with no entry in roleDefinitionsMap (a dangling parent
 * reference) - turning a merely-logged data anomaly into a hard failure of the closure
 * computation callers (and, in production, of every capability check for whoever held
 * that role).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProjectRoleDefinitionsManager_TestCase {

    @Mock
    private ProjectRoleDefinitionsRepository repository;

    private static RoleDefinition role(String id, String... parents) {
        return RoleDefinition.get(RoleId.valueOf(id),
                                  RoleType.PROJECT_ROLE,
                                  new LinkedHashSet<>(Set.of(parents)).stream()
                                          .map(RoleId::valueOf)
                                          .collect(Collectors.toCollection(LinkedHashSet::new)),
                                  Set.of(),
                                  id,
                                  id);
    }

    @Test
    public void shouldNotThrowWhenADanglingParentReferenceIsReachedWhileLoggingATriggeredCycle() {
        // START has two children (A, B, D) that all share parent C, so C is enqueued
        // three times - the second dequeue of C is treated as a "cycle" (the code
        // cannot distinguish a real cycle from a shared-ancestor diamond) and triggers
        // the cycle-logging path. C's own parent, "Z", is deliberately never defined as
        // a role - reproducing the dangling reference the crash needs.
        var start = role("START", "A", "B", "D");
        var a = role("A", "C");
        var b = role("B", "C");
        var d = role("D", "C");
        var c = role("C", "Z");
        var projectId = ProjectId.valueOf("11111111-1111-1111-1111-111111111111");
        when(repository.getProjectRoleDefinitions(projectId))
                .thenReturn(Optional.of(ProjectRoleDefinitionsRecord.get(projectId, Set.of(start, a, b, d, c))));

        var manager = new ProjectRoleDefinitionsManager(repository);

        var closure = assertDoesNotThrow(
                () -> manager.getProjectRoleClosure(projectId, RoleId.valueOf("START")));

        // "Z" was never resolvable to a RoleDefinition, so it is absent from the
        // closure - the outer algorithm already tolerates that gracefully; only the
        // logging reconstruction needed fixing.
        assertEquals(Set.of(start, a, b, d, c), closure);
    }
}
