package edu.stanford.protege.webprotege.authorization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.protege.webprotege.WebProtegeCommonConfiguration;
import edu.stanford.protege.webprotege.cmd.CommandExecutor;
import edu.stanford.protege.webprotege.model.ProjectId;
import edu.stanford.protege.webprotege.model.UserId;
import kafka.utils.Json;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2021-07-29
 */
@SpringBootTest
@Import(WebProtegeCommonConfiguration.class)
public class GetUserRolesRequestHandler_Tests {

    @TestConfiguration
    static class Config {

        @Bean
        CommandExecutor<GetUserRolesRequest, GetUserRolesResponse> executor() {
            return new CommandExecutor<>(GetUserRolesResponse.class);
        }
    }

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    GetUserRolesRequestHandler handler;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    CommandExecutor<GetUserRolesRequest, GetUserRolesResponse> commandExecutor;

    @Autowired
    private GetUserRolesRequestHandler handlerSpy;

    @BeforeEach
    void setUp() {
        handlerSpy = spy(handler);
    }

    @Test
    void shouldReceiveMessage() throws InterruptedException, IOException, ExecutionException {
        var fred_smith = UserId.valueOf("Fred Smith");
        var resource = new ProjectResource(new ProjectId("11111111-2222-2222-2222-333333333333"));
        var request = new GetUserRolesRequest(fred_smith, resource);
        var responseFuture = commandExecutor.execute(request);
        var response = responseFuture.get();
        assertThat(response.userId()).isEqualTo(fred_smith);
        assertThat(response.resource()).isEqualTo(resource);
    }
}
