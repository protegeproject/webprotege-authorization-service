package edu.stanford.protege.webprotege.authorization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.dockerjava.api.DockerClient;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ServerInfoResource;
import org.keycloak.representations.idm.RealmRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.startupcheck.OneShotStartupCheckStrategy;
import org.testcontainers.containers.startupcheck.StartupCheckStrategy;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.Duration;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2024-03-26
 */
public class KeycloakTestExtension implements BeforeAllCallback, AfterAllCallback {

    @Container
    private KeycloakContainer keycloakContainer;

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        keycloakContainer = new KeycloakContainer("quay.io/keycloak/keycloak:24.0")
                .withRealmImportFile("/keycloak/webprotege-realm.json");
        keycloakContainer.start();

        var url = keycloakContainer.getAuthServerUrl();

        System.setProperty("keycloak-issuer-url",
                           url + "/realms/webprotege/protocol/openid-connect/certs");

//        System.setProperty("spring.security.oauth2.client.registration.keycloak.redirectUri",
//                           url + "/realms/webprotege");
//
//        System.setProperty("spring.security.oauth2.client.provider.keycloak.authorizationUri",
//                           url + "/realms/webprotege/protocol/openid-connect/token");
//
//        System.setProperty("spring.security.oauth2.client.provider.keycloak.tokenUri",
//                           url + "/realms/webprotege/protocol/openid-connect/token");
//
//        System.setProperty("spring.security.oauth2.resourceserver.jwt.issuer-uri",
//                           url + "/realms/webprotege");
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        keycloakContainer.stop();
    }
}
