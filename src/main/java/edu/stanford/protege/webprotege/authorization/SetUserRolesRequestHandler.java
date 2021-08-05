package edu.stanford.protege.webprotege.authorization;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2021-07-30
 */
@Component("SetUserRolesRequestHandler")
public class SetUserRolesRequestHandler {

    private final AccessManager accessManager;

    public SetUserRolesRequestHandler(AccessManager accessManager) {
        this.accessManager = accessManager;
    }

    @KafkaListener(groupId = "${spring.application.name}", topics = "SetUserRoles")
    protected SetUserRolesResponse handleAction(SetUserRolesRequest action) {
        accessManager.setAssignedRoles(Subject.forUser(action.userId()),
                                       action.resource(),
                                       action.roles());
        return new SetUserRolesResponse();
    }
}
