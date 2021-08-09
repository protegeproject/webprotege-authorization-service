package edu.stanford.protege.webprotege.authorization;

import edu.stanford.protege.webprotege.authorization.api.GetAuthorizedActionsRequest;
import edu.stanford.protege.webprotege.authorization.api.GetAuthorizedActionsResponse;
import edu.stanford.protege.webprotege.ipc.CommandHandler;
import edu.stanford.protege.webprotege.ipc.WebProtegeHandler;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2021-08-09
 */
@WebProtegeHandler
public class GetAuthorizedActionsHandler implements CommandHandler<GetAuthorizedActionsRequest, GetAuthorizedActionsResponse> {

    private final AccessManager accessManager;

    public GetAuthorizedActionsHandler(AccessManager accessManager) {
        this.accessManager = accessManager;
    }

    @Nonnull
    @Override
    public String getChannelName() {
        return GetAuthorizedActionsRequest.CHANNEL_NAME;
    }

    @Override
    public Class<GetAuthorizedActionsRequest> getRequestClass() {
        return GetAuthorizedActionsRequest.class;
    }

    @Override
    public Mono<GetAuthorizedActionsResponse> handleRequest(GetAuthorizedActionsRequest request) {
        var actionClosure = accessManager.getActionClosure(request.subject(),
                                       request.resource());
        return Mono.just(new GetAuthorizedActionsResponse(request.resource(),
                                                          request.subject(),
                                                          actionClosure));
    }
}
