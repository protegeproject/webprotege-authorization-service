package edu.stanford.protege.webprotege.authorization;

import javax.annotation.Nonnull;
import java.util.Collection;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 4 Jan 2017
 *
 * Provides access to the role closure for a given role id.
 */
public interface RoleOracle {

    @Nonnull
    Collection<Role> getRoleClosure(@Nonnull RoleId roleId);

    Collection<ActionId> getActionsAssociatedToRoles(Collection<RoleId> roleIds);

}

