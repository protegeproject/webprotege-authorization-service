package edu.stanford.protege.webprotege.authorization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2021-07-29
 */
@Component("GetUserRolesRequestHandler")
public class GetUserRolesRequestHandler {

    private final AccessManager accessManager;

    private final ObjectMapper objectMapper;

    public GetUserRolesRequestHandler(AccessManager accessManager, ObjectMapper objectMapper) {
        this.accessManager = accessManager;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(groupId = "${spring.application.name}", topics = "GetUserRoles")
    @SendTo
    protected String handleAction(String request) throws JsonProcessingException {
        var action = objectMapper.readValue(request, GetUserRolesRequest.class);
        var assignedRoles = accessManager.getAssignedRoles(
                Subject.forUser(action.userId()),
                action.resource());
        var response = new GetUserRolesResponse(action.resource(),
                                        action.userId(),
                                        assignedRoles);
        return objectMapper.writeValueAsString(response);
    }
}
