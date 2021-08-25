package edu.stanford.protege.webprotege.authorization;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2021-07-29
 */
@Configuration
public class MongoConfiguration extends AbstractMongoClientConfiguration {

    @Value("${webprotege.database.name}")
    private String databaseName;

    @Override
    protected String getDatabaseName() {
        return databaseName;
    }
}
