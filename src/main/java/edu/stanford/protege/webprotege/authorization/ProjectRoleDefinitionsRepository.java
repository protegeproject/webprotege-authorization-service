package edu.stanford.protege.webprotege.authorization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.model.ReplaceOptions;
import edu.stanford.protege.webprotege.common.ProjectId;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ProjectRoleDefinitionsRepository {
    private static final String COLLECTION_NAME = "ProjectRoleDefinitions";

    private final MongoTemplate mongoTemplate;

    private final ObjectMapper objectMapper;

    public ProjectRoleDefinitionsRepository(MongoTemplate mongoTemplate, ObjectMapper objectMapper) {
        this.mongoTemplate = mongoTemplate;
        this.objectMapper = objectMapper;
    }

    public synchronized void saveProjectRoleDefinitions(ProjectRoleDefinitionsRecord record) {
        var document = objectMapper.convertValue(record, Document.class);
        var collection = mongoTemplate.getCollection(COLLECTION_NAME);
        var query = new Document("_id", record.projectId().value());
        collection.replaceOne(query, document, new ReplaceOptions().upsert(true));
    }

    public synchronized void clearProjectRoleDefinitions(ProjectId projectId) {
        var collection = mongoTemplate.getCollection(COLLECTION_NAME);
        collection.deleteOne(new Document("_id", projectId.value()));
    }

    public synchronized Optional<ProjectRoleDefinitionsRecord> getProjectRoleDefinitions(ProjectId projectId) {
        var query = new Document("_id", projectId.value());
        var collection = mongoTemplate.getCollection(COLLECTION_NAME);
        var found = collection.find(query).first();
        if(found == null) {
            return Optional.empty();
        }
        else {
            var record = objectMapper.convertValue(found, ProjectRoleDefinitionsRecord.class);
            return Optional.of(record);
        }
    }


}
