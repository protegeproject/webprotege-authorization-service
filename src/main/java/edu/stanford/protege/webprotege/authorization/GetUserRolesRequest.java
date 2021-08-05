package edu.stanford.protege.webprotege.authorization;

import edu.stanford.protege.webprotege.cmd.Request;
import edu.stanford.protege.webprotege.model.UserId;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2021-07-29
 */
public record GetUserRolesRequest(UserId userId, Resource resource) implements Request<GetUserRolesResponse> {

    @Override
    public String getChannel() {
        return "GetUserRoles";
    }
}
