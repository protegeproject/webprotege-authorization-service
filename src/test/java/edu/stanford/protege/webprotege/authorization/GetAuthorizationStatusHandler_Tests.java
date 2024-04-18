package edu.stanford.protege.webprotege.authorization;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.common.UserId;
import edu.stanford.protege.webprotege.common.WebProtegeCommonConfiguration;
import edu.stanford.protege.webprotege.ipc.CommandExecutor;
import edu.stanford.protege.webprotege.ipc.ExecutionContext;
import edu.stanford.protege.webprotege.ipc.WebProtegeIpcApplication;
import edu.stanford.protege.webprotege.ipc.impl.CommandExecutorImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2024-03-27
 */
@SpringBootTest
@Import({WebProtegeCommonConfiguration.class, WebProtegeIpcApplication.class})
@ExtendWith({KeycloakTestExtension.class, MongoTestExtension.class, RabbitMqTestExtension.class})
public class GetAuthorizationStatusHandler_Tests {


    @Autowired
    GetAuthorizationStatusHandler handler;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    CommandExecutor<GetAuthorizationStatusRequest, GetAuthorizationStatusResponse> commandExecutor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        MockitoAnnotations.openMocks(this).close();
    }

    @Test
    void shouldExecuteCommand() throws InterruptedException, ExecutionException {
        var subject = Subject.forUser(UserId.valueOf("Fred Smith"));
        var resource = new ProjectResource(ProjectId.generate());
        var request = new GetAuthorizationStatusRequest(resource, subject, BuiltInAction.CREATE_CLASS.getActionId());
        var responseFuture = commandExecutor.execute(request, new ExecutionContext());
        var response = responseFuture.get();
        assertThat(response.subject()).isEqualTo(subject);
        assertThat(response.resource()).isEqualTo(resource);
        assertThat(response.authorizationStatus()).isEqualTo(AuthorizationStatus.UNAUTHORIZED);
    }

}
