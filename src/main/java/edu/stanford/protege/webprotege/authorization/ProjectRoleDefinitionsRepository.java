package edu.stanford.protege.webprotege.authorization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.model.ReplaceOptions;
import edu.stanford.protege.webprotege.common.ProjectId;
import org.bson.Document;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Component
public class ProjectRoleDefinitionsRepository {

    private static final String COLLECTION_NAME = "ProjectRoleDefinitions";
    private static final String REVISIONS_COLLECTION_NAME = "ProjectRoleDefinitions_revisions";
    private static final String CACHE_NAME = "projectRoleDefinitions";

    private final MongoTemplate mongoTemplate;
    private final ObjectMapper objectMapper;

    public ProjectRoleDefinitionsRepository(MongoTemplate mongoTemplate, ObjectMapper objectMapper) {
        this.mongoTemplate = mongoTemplate;
        this.objectMapper = objectMapper;
    }

    @CacheEvict(value = CACHE_NAME, key = "#record.projectId().value()")
    public synchronized void saveProjectRoleDefinitions(ProjectRoleDefinitionsRecord record) {
        var document = objectMapper.convertValue(record, Document.class);
        document.put("_id", record.projectId().value());
        document.remove("projectId");
        var collection = mongoTemplate.getCollection(COLLECTION_NAME);
        var query = new Document("_id", record.projectId().value());
        collection.replaceOne(query, document, new ReplaceOptions().upsert(true));
        var revisionsCollection = mongoTemplate.getCollection(REVISIONS_COLLECTION_NAME);
        document.put("projectId", record.projectId().value());
        document.remove("_id");
        var dateTime = Instant.now();
        var dateTimeStamp = DateTimeFormatter.ISO_INSTANT.format(dateTime);
        document.put("revisionDateTime", dateTimeStamp);
        revisionsCollection.insertOne(document);
    }

    private static Integer getRevisionNumber(@Nullable Document document) {
        if(document == null) {
            return 1;
        }
        var revisionNumber = document.getInteger("revision");
        if(revisionNumber == null) {
            return 1;
        }
        else {
            return revisionNumber;
        }
    }

    @CacheEvict(value = CACHE_NAME, key = "#projectId.value()")
    public synchronized void clearProjectRoleDefinitions(ProjectId projectId) {
        var collection = mongoTemplate.getCollection(COLLECTION_NAME);
        collection.deleteOne(new Document("_id", projectId.value()));
    }

    @Cacheable(value = CACHE_NAME, key = "#projectId.value()", unless = "#result == null")
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
