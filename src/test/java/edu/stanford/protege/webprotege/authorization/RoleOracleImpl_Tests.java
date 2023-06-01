package edu.stanford.protege.webprotege.authorization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2023-06-01
 */
public class RoleOracleImpl_Tests {

    private RoleOracleImpl roleOracle;


    @BeforeEach
    void setUp() {
        roleOracle = RoleOracleImpl.get();
    }

    @Test
    void shouldContainRoleAncestors() {
        var closure = roleOracle.getRoleClosure(BuiltInRole.PROJECT_EDITOR.getRoleId());
        assertThat(closure).anyMatch(role -> role.roleId().equals(BuiltInRole.PROJECT_VIEWER.getRoleId()));
    }
}
