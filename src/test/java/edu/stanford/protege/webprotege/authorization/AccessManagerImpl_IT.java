package edu.stanford.protege.webprotege.authorization;

import com.mongodb.client.MongoCollection;
import edu.stanford.protege.webprotege.ipc.WebProtegeIpcApplication;
import edu.stanford.protege.webprotege.ipc.impl.RabbitMqConfiguration;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ExtendWith({MongoTestExtension.class, RabbitMqTestExtension.class, KeycloakTestExtension.class})
class AccessManagerImpl_IT {

    private static final String THE_USER_NAME = "The User";

    private static final String USER_NAME_FIELD = "userName";

    private static final String ASSIGNED_ROLES_FIELD = "assignedRoles";

    private static final String ROLE_CLOSURE_FIELD = "roleClosure";

    private static final String ACTION_CLOSURE_FIELD = "actionClosure";

    @Autowired
    private AccessManagerImpl manager;

    @Autowired
    private MongoTemplate mongoTemplate;

    private Subject subject;

    private ApplicationResource resource;

    private Set<RoleId> assignedRoles;

    private Document storedDocument;

    private Document userQuery;

    @BeforeEach
    public void setUp() throws Exception {
        getCollection().drop();
        subject = Subject.forUser(THE_USER_NAME);
        resource = ApplicationResource.get();
        assignedRoles = Collections.singleton(BuiltInRole.CAN_COMMENT.getRoleId());
        userQuery = new Document(USER_NAME_FIELD, THE_USER_NAME);
        manager.setAssignedRoles(
                subject,
                resource,
                assignedRoles
        );
        storedDocument = getCollection().find(userQuery).first();
    }

    @Test
    public void shouldStoreAssignedRoles() {
        assertThat(storedDocument, is(notNullValue()));
        assertThat((List<String>) storedDocument.get(ASSIGNED_ROLES_FIELD), hasItems("CanComment"));
    }

    @Test
    public void shouldStoreRoleClosure() {
        assertThat(storedDocument, is(notNullValue()));
        // Just check it contains a parent role
        assertThat((List<String>) storedDocument.get(ROLE_CLOSURE_FIELD), hasItems("CanView"));
    }

    @Test
    public void shouldStoreActionClosure() {
        assertThat(storedDocument, is(notNullValue()));
        assertThat((List<String>) storedDocument.get(ACTION_CLOSURE_FIELD), hasItems("ViewProject"));
    }

    @Test
    public void shouldNotStoreDuplicate() {
        manager.setAssignedRoles(
                subject,
                resource,
                assignedRoles
        );
        assertThat(countDocuments(), is(1L));
    }

    private long countDocuments() {
        return getCollection().countDocuments();
    }

    private MongoCollection<Document> getCollection() {
        return mongoTemplate.getCollection("RoleAssignments");
    }

    @Test
    public void shouldRebuildRoleClosure() {
        getCollection().updateOne(userQuery, new Document("$set", new Document("roleClosure", emptyList())));
        getCollection().updateOne(userQuery, new Document("$set", new Document("actionClosure", emptyList())));
        manager.rebuild();
        Document rebuiltDocument = getCollection().find().first();
        assertThat((List<String>) rebuiltDocument.get(ROLE_CLOSURE_FIELD), hasItems("CanView"));
    }

    @Test
    public void shouldRebuildActionClosure() {
        getCollection().updateOne(userQuery, new Document("$set", new Document("roleClosure", emptyList())));
        getCollection().updateOne(userQuery, new Document("$set", new Document("actionClosure", emptyList())));
        manager.rebuild();
        Document rebuiltDocument = getCollection().find().first();
        assertThat((List<String>) rebuiltDocument.get(ACTION_CLOSURE_FIELD), hasItems("ViewProject"));
    }

    @AfterEach
    public void tearDown() {
        getCollection().drop();
    }
}