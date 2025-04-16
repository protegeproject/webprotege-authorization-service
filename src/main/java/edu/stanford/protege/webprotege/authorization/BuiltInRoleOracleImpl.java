package edu.stanford.protege.webprotege.authorization;

import javax.annotation.Nonnull;
import java.util.*;

import static java.util.stream.Collectors.toList;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 5 Jan 2017
 */
public class BuiltInRoleOracleImpl implements BuiltInRoleOracle {

    private Map<RoleId, RoleDefinition> closure = new LinkedHashMap<>();

    private BuiltInRoleOracleImpl() {

    }

    public static BuiltInRoleOracleImpl get() {
        BuiltInRoleOracleImpl impl = new BuiltInRoleOracleImpl();
        for(BuiltInRole builtInRole : BuiltInRole.values()) {
            List<RoleId> parentRoles = builtInRole.getParents().stream()
                                                  .map(BuiltInRole::getRoleId)
                                                  .collect(toList());
            List<Capability> capabilities = new ArrayList<>(builtInRole.getCapabilities());
            impl.addRole(RoleDefinition.get(builtInRole.getRoleId(), builtInRole.getRoleType(), Set.copyOf(parentRoles), Set.copyOf(capabilities), ""));
        }
        return impl;
    }

    @Nonnull
    @Override
    public Collection<RoleDefinition> getRoleClosure(@Nonnull RoleId roleId) {
        Set<RoleDefinition> result = new HashSet<>();
        add(roleId, result);
        return result;
    }

    @Override
    public Collection<Capability> getCapabilitiesAssociatedToRoles(Collection<RoleId> roleIds) {
        return roleIds.stream()
                .flatMap(id -> getRoleClosure(id).stream())
                .flatMap(r -> r.roleCapabilities().stream())
                .collect(toList());
    }

    private void add(RoleId roleId, Set<RoleDefinition> result) {
        RoleDefinition role = closure.get(roleId);
        if(role == null) {
            return;
        }
        if(result.add(role)) {
            role.parentRoles().forEach(pr -> add(pr, result));
        }
    }

    private void addRole(RoleDefinition role) {
        closure.put(role.roleId(), role);
    }
}
