package edu.stanford.protege.webprotege.authorization;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;

import java.time.Duration;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2024-03-26
 */
public class RabbitMqTestExtension implements BeforeAllCallback, AfterAllCallback {

    @Container
    private RabbitMQContainer rabbitMQContainer;

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        rabbitMQContainer = new RabbitMQContainer("rabbitmq:3.7.25-management-alpine")
                .withStartupTimeout(Duration.ofSeconds(60)) // Increase startup timeout if needed
                .waitingFor(
                        Wait.forLogMessage(".*Server startup complete.*\\n", 1)
                );
        rabbitMQContainer.start();
        System.setProperty("spring.rabbitmq.host", rabbitMQContainer.getHost());
        System.setProperty("spring.rabbitmq.port", Integer.toString(rabbitMQContainer.getAmqpPort()));
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        rabbitMQContainer.stop();
    }

}
