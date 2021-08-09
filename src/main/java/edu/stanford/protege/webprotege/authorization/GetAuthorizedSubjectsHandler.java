package edu.stanford.protege.webprotege.authorization;

import edu.stanford.protege.webprotege.authorization.api.GetAuthorizedSubjectsRequest;
import edu.stanford.protege.webprotege.authorization.api.GetAuthorizedSubjectsResponse;
import edu.stanford.protege.webprotege.ipc.CommandHandler;
import edu.stanford.protege.webprotege.ipc.WebProtegeHandler;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2021-08-09
 */
@WebProtegeHandler
public class GetAuthorizedSubjectsHandler implements CommandHandler<GetAuthorizedSubjectsRequest, GetAuthorizedSubjectsResponse> {

    private final AccessManager accessManager;

    public GetAuthorizedSubjectsHandler(AccessManager accessManager) {
        this.accessManager = accessManager;
    }

    @Nonnull
    @Override
    public String getChannelName() {
        return GetAuthorizedSubjectsRequest.CHANNEL_NAME;
    }

    @Override
    public Class<GetAuthorizedSubjectsRequest> getRequestClass() {
        return GetAuthorizedSubjectsRequest.class;
    }

    @Override
    public Mono<GetAuthorizedSubjectsResponse> handleRequest(GetAuthorizedSubjectsRequest request) {
        var subjects = accessManager.getSubjectsWithAccessToResource(request.resource(),
                                                      request.actionId());

        return Mono.just(new GetAuthorizedSubjectsResponse(request.resource(),
                                                           request.actionId(),
                                                           Set.copyOf(subjects)));
    }
}
