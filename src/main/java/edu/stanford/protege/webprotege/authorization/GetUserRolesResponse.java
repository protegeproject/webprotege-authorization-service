package edu.stanford.protege.webprotege.authorization;


import edu.stanford.protege.webprotege.cmd.Response;
import edu.stanford.protege.webprotege.model.UserId;

import java.util.Collection;

public record GetUserRolesResponse(Resource resource,
                                   UserId userId,
                                   Collection<RoleId> roleIds) implements Response {

}
