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
import org.keycloak.common.VerificationException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2021-07-29
 */
@SpringBootTest
@Import({WebProtegeCommonConfiguration.class, WebProtegeIpcApplication.class})
public class GetRolesCommandHandler_Tests extends IntegrationTestsExtension {


    @Mock
    TokenValidator tokenValidator;

    @TestConfiguration
    static class Config {

        @Bean
        CommandExecutor<GetRolesRequest, GetRolesResponse> executor() {
            return new CommandExecutorImpl<>(GetRolesResponse.class);
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
        MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        MockitoAnnotations.openMocks(this).close();
    }

    @Test
    void shouldExecuteCommand() throws InterruptedException, IOException, ExecutionException, VerificationException, CertificateException {
        var subject = Subject.forUser(UserId.valueOf("Fred Smith"));
        var resource = new ProjectResource(ProjectId.generate());
        var request = new GetRolesRequest(subject, resource);
        var executionContext = new ExecutionContext();
        var responseFuture = commandExecutor.execute(request, new ExecutionContext());
        var response = responseFuture.get();
        assertThat(response.subject()).isEqualTo(subject);
        assertThat(response.resource()).isEqualTo(resource);
    }
}
