package edu.stanford.protege.webprotege.authorization;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 4 Jan 2017
 *
 * Provides access to the role closure for a given built in role id.
 */
public interface BuiltInRoleOracle {

    @Nonnull
    Collection<RoleDefinition> getRoleClosure(@Nonnull RoleId roleId);

    Collection<Capability> getCapabilitiesAssociatedToRoles(Collection<RoleId> roleIds);

}

