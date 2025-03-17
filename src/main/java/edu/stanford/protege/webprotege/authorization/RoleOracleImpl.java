package edu.stanford.protege.webprotege.authorization;

import javax.annotation.Nonnull;
import java.security.DrbgParameters;
import java.util.*;

import static java.util.stream.Collectors.toList;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 5 Jan 2017
 */
public class RoleOracleImpl implements RoleOracle {

    private Map<RoleId, Role> closure = new LinkedHashMap<>();

    private RoleOracleImpl() {

    }

    public static RoleOracleImpl get() {
        RoleOracleImpl impl = new RoleOracleImpl();
        for(BuiltInRole builtInRole : BuiltInRole.values()) {
            List<RoleId> parentRoles = builtInRole.getParents().stream()
                                                  .map(BuiltInRole::getRoleId)
                                                  .collect(toList());
            List<Capability> capabilities = new ArrayList<>(builtInRole.getCapabilities());
            impl.addRole(new Role(builtInRole.getRoleId(), parentRoles, capabilities));
        }
        return impl;
    }

    @Nonnull
    @Override
    public Collection<Role> getRoleClosure(@Nonnull RoleId roleId) {
        Set<Role> result = new HashSet<>();
        add(roleId, result);
        return result;
    }

    @Override
    public Collection<Capability> getCapabilitiesAssociatedToRoles(Collection<RoleId> roleIds) {
        return roleIds.stream()
                .flatMap(id -> getRoleClosure(id).stream())
                .flatMap(r -> r.capabilities().stream())
                .collect(toList());
    }

    private void add(RoleId roleId, Set<Role> result) {
        Role role = closure.get(roleId);
        if(role == null) {
            return;
        }
        if(result.add(role)) {
            role.parents().forEach(pr -> add(pr, result));
        }
    }

    private void addRole(Role role) {
        closure.put(role.roleId(), role);
    }
}
