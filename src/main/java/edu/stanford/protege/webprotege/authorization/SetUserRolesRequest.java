package edu.stanford.protege.webprotege.authorization;


import edu.stanford.protege.webprotege.cmd.Request;
import edu.stanford.protege.webprotege.model.UserId;

import java.util.Set;

public record SetUserRolesRequest(UserId userId, Resource resource, Set<RoleId> roles) implements Request<SetUserRolesResponse> {

    @Override
    public String getChannel() {
        return null;
    }
}
