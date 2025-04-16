package edu.stanford.protege.webprotege.authorization;

import edu.stanford.protege.webprotege.ipc.CommandHandler;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
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
        return GetAuthorizedResourcesRequest.CHANNEL;
    }

    @Override
    public Class<GetAuthorizedResourcesRequest> getRequestClass() {
        return GetAuthorizedResourcesRequest.class;
    }

    @Override
    public Mono<GetAuthorizedResourcesResponse> handleRequest(GetAuthorizedResourcesRequest request, ExecutionContext executionContext) {
        var resources = accessManager.getResourcesAccessibleToSubject(request.subject(),
                                                      request.capability());

        return Mono.just(new GetAuthorizedResourcesResponse(request.subject(),
                                                            request.capability(),
                                                            Set.copyOf(resources)));
    }
}
