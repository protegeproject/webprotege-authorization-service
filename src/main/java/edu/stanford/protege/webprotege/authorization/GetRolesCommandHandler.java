package edu.stanford.protege.webprotege.authorization;

import edu.stanford.protege.webprotege.authorization.api.GetRolesRequest;
import edu.stanford.protege.webprotege.authorization.api.GetRolesResponse;
import edu.stanford.protege.webprotege.ipc.CommandHandler;
import edu.stanford.protege.webprotege.ipc.WebProtegeHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2021-07-29
 */
@WebProtegeHandler
public class GetRolesCommandHandler implements CommandHandler<GetRolesRequest, GetRolesResponse> {

    private final AccessManager accessManager;

    public GetRolesCommandHandler(AccessManager accessManager) {
        this.accessManager = accessManager;
    }

    @Nonnull
    @Override
    public String getChannelName() {
        return GetRolesRequest.CHANNEL_NAME;
    }

    @Override
    public Class<GetRolesRequest> getRequestClass() {
        return GetRolesRequest.class;
    }

    @Override
    public Mono<GetRolesResponse> handleRequest(GetRolesRequest request) {
        var roleClosure = accessManager.getRoleClosure(
                request.subject(),
                request.resource());
        var response = new GetRolesResponse(request.subject(),
                                            request.resource(),
                                            Set.copyOf(roleClosure));
        return Mono.just(response);
    }
}
