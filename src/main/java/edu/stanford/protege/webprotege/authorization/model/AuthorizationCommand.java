package edu.stanford.protege.webprotege.authorization.model;

import edu.stanford.protege.webprotege.common.Request;
import edu.stanford.protege.webprotege.common.Response;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;

public record AuthorizationCommand<Req extends Request<Resp>, Resp extends Response>(Req request, ExecutionContext context) {
}
