package edu.stanford.protege.webprotege.authorization;

import java.util.List;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 5 Jan 2017
 *
 * A role associates a role Id with a set of actions that can be performed.  Hierarchical roles are supported.
 */
public record Role(RoleId roleId,
                   List<RoleId> parents,
                   List<ActionId> actions) {

    public Role {
        parents = List.copyOf(parents);
        actions = List.copyOf(actions);
    }
}
