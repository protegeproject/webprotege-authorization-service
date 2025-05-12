package edu.stanford.protege.webprotege.authorization;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.protege.webprotege.common.ProjectId;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.util.ISO8601DateFormat;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "webprotege.rabbitmq.commands-subscribe=false")
@ExtendWith(MongoTestExtension.class)
class ProjectRoleDefinitionsRepository_IT {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private ProjectRoleDefinitionsRepository repository;

    private List<RoleDefinition> roleDefinitions;

    @BeforeEach
    void setUp() {
        mongoTemplate.getCollection("ProjectRoleDefinitions").drop();
        repository = new ProjectRoleDefinitionsRepository(mongoTemplate, objectMapper);
        roleDefinitions = List.of(
            new RoleDefinition(RoleId.valueOf("ProjectEditor"), RoleType.PROJECT_ROLE, Set.of(RoleId.valueOf("ProjectViewer")), Set.of(), "Testing label","Testing role")
        );
    }

    @Test
    void shouldSaveAndRetrieveRoleDefinitions() {
        // Given
        var projectId = ProjectId.valueOf("12345678-1234-5678-1234-567812345678");

        // When
        repository.saveProjectRoleDefinitions(ProjectRoleDefinitionsRecord.get(projectId, roleDefinitions));
        var retrieved = repository.getProjectRoleDefinitions(projectId);

        // Then
        assertThat(retrieved).contains(ProjectRoleDefinitionsRecord.get(projectId, roleDefinitions));

        var revisionsCollection = mongoTemplate.getCollection("ProjectRoleDefinitions_revisions");
        var revision = revisionsCollection.find(new Document("projectId", projectId.value())).first();
        assertThat(revision).isNotNull();
        var revisionDateTime = revision.getString("revisionDateTime" );
        assertThat(revisionDateTime).isNotNull();
        assertThat(revisionDateTime).satisfies(dt -> OffsetDateTime.parse(dt, DateTimeFormatter.ISO_DATE_TIME));
    }

    @Test
    void shouldReturnEmptyListWhenNoRoleDefinitionsExist() {
        // Given
        var projectId = ProjectId.valueOf("87654321-4321-8765-4321-876543210987");

        // When
        var retrieved = repository.getProjectRoleDefinitions(projectId);

        // Then
        assertThat(retrieved).isEmpty();
    }

    @Test
    void shouldClearRoleDefinitions() {
        // Given
        var projectId = ProjectId.valueOf("11111111-2222-3333-4444-555555555555");
        repository.saveProjectRoleDefinitions(ProjectRoleDefinitionsRecord.get(projectId, roleDefinitions));

        // When
        repository.clearProjectRoleDefinitions(projectId);
        var retrieved = repository.getProjectRoleDefinitions(projectId);

        // Then
        assertThat(retrieved).isEmpty();
    }

    @Test
    void shouldUpdateExistingRoleDefinitions() {
        // Given
        var projectId = ProjectId.valueOf("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
        var initialDefinitions = roleDefinitions;
        var updatedDefinitions = List.of(
                new RoleDefinition(RoleId.valueOf("ProjectEditor"), RoleType.PROJECT_ROLE, Set.of(RoleId.valueOf("ProjectViewer")), Set.of(), "Testing label","Testing role"),
                new RoleDefinition(RoleId.valueOf("ProjectManager"), RoleType.PROJECT_ROLE, Set.of(RoleId.valueOf("ProjectEditor")), Set.of(), "Testing label", "Testing role")
        );

        // When
        repository.saveProjectRoleDefinitions(ProjectRoleDefinitionsRecord.get(projectId, initialDefinitions));
        repository.saveProjectRoleDefinitions(ProjectRoleDefinitionsRecord.get(projectId, updatedDefinitions));
        var retrieved = repository.getProjectRoleDefinitions(projectId);

        // Then
        assertThat(retrieved).contains(ProjectRoleDefinitionsRecord.get(projectId, updatedDefinitions));
    }

    @Test
    void shouldNotSaveDuplicates() {
        var projectId = ProjectId.valueOf("12345678-1234-5678-1234-567812345678");

        repository.saveProjectRoleDefinitions(ProjectRoleDefinitionsRecord.get(projectId, roleDefinitions));
        repository.saveProjectRoleDefinitions(ProjectRoleDefinitionsRecord.get(projectId, roleDefinitions));
        var retrieved = repository.getProjectRoleDefinitions(projectId);

        var collection = mongoTemplate.getCollection("ProjectRoleDefinitions");
        var count = collection.countDocuments();

        assertThat(count).isEqualTo(1);
    }
}