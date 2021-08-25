package edu.stanford.protege.webprotege.authorization;

import edu.stanford.protege.webprotege.ipc.CommandHandler;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import edu.stanford.protege.webprotege.ipc.WebProtegeHandler;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2021-08-09
 */
@WebProtegeHandler
public class GetAuthorizationStatusHandler implements CommandHandler<GetAuthorizationStatusRequest, GetAuthorizationStatusResponse> {

    private final AccessManager accessManager;

    public GetAuthorizationStatusHandler(AccessManager accessManager) {
        this.accessManager = accessManager;
    }

    @Nonnull
    @Override
    public String getChannelName() {
        return GetAuthorizationStatusRequest.CHANNEL;
    }

    @Override
    public Class<GetAuthorizationStatusRequest> getRequestClass() {
        return GetAuthorizationStatusRequest.class;
    }

    @Override
    public Mono<GetAuthorizationStatusResponse> handleRequest(GetAuthorizationStatusRequest request, ExecutionContext executionContext) {
        var hasPermission = accessManager.hasPermission(request.subject(),
                                    request.resource(),
                                    request.actionId());

        if(hasPermission) {
            return Mono.just(new GetAuthorizationStatusResponse(request.resource(),
                                                                request.subject(),
                                                                AuthorizationStatus.AUTHORIZED));
        }
        else {
            return Mono.just(new GetAuthorizationStatusResponse(request.resource(),
                                                                request.subject(),
                                                                AuthorizationStatus.UNAUTHORIZED));
        }
    }
}
