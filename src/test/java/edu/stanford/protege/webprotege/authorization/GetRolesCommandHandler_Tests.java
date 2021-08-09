package edu.stanford.protege.webprotege.authorization;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.protege.webprotege.authorization.api.GetRolesRequest;
import edu.stanford.protege.webprotege.authorization.api.GetRolesResponse;
import edu.stanford.protege.webprotege.authorization.api.ProjectResource;
import edu.stanford.protege.webprotege.authorization.api.Subject;
import edu.stanford.protege.webprotege.common.ProjectId;
import edu.stanford.protege.webprotege.common.UserId;
import edu.stanford.protege.webprotege.common.WebProtegeCommonConfiguration;
import edu.stanford.protege.webprotege.ipc.CommandExecutor;
import edu.stanford.protege.webprotege.ipc.WebProtegeIpcApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2021-07-29
 */
@SpringBootTest
@Import({WebProtegeCommonConfiguration.class, WebProtegeIpcApplication.class})
public class GetRolesCommandHandler_Tests {

    @TestConfiguration
    static class Config {

        @Bean
        CommandExecutor<GetRolesRequest, GetRolesResponse> executor() {
            return new CommandExecutor<>(GetRolesResponse.class);
        }
    }

    @Autowired
    GetRolesCommandHandler handler;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    CommandExecutor<GetRolesRequest, GetRolesResponse> commandExecutor;

    @BeforeEach
    void setUp() {
    }

    @Test
    void shouldExecuteCommand() throws InterruptedException, IOException, ExecutionException {
        var subject = Subject.forUser(UserId.valueOf("Fred Smith"));
        var resource = new ProjectResource(ProjectId.generate());
        var request = new GetRolesRequest(subject, resource);
        var responseFuture = commandExecutor.execute(request);
        var response = responseFuture.get();
        assertThat(response.subject()).isEqualTo(subject);
        assertThat(response.resource()).isEqualTo(resource);
    }
}
