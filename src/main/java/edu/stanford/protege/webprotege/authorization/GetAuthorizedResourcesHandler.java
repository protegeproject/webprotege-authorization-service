package edu.stanford.protege.webprotege.authorization;

import edu.stanford.protege.webprotege.authorization.api.GetAuthorizedResourcesRequest;
import edu.stanford.protege.webprotege.authorization.api.GetAuthorizedResourcesResponse;
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
public class GetAuthorizedResourcesHandler implements CommandHandler<GetAuthorizedResourcesRequest, GetAuthorizedResourcesResponse> {

    private final AccessManager accessManager;

    public GetAuthorizedResourcesHandler(AccessManager accessManager) {
        this.accessManager = accessManager;
    }

    @Nonnull
    @Override
    public String getChannelName() {
        return GetAuthorizedResourcesRequest.CHANNEL_NAME;
    }

    @Override
    public Class<GetAuthorizedResourcesRequest> getRequestClass() {
        return GetAuthorizedResourcesRequest.class;
    }

    @Override
    public Mono<GetAuthorizedResourcesResponse> handleRequest(GetAuthorizedResourcesRequest request) {
        var resources = accessManager.getResourcesAccessibleToSubject(request.subject(),
                                                      request.actionId());

        return Mono.just(new GetAuthorizedResourcesResponse(request.subject(),
                                                            request.actionId(),
                                                            Set.copyOf(resources)));
    }
}
