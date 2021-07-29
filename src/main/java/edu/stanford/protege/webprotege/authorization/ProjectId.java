package edu.stanford.protege.webprotege.authorization;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2021-07-28
 */
public record ProjectId(String id) {

    @JsonValue
    @Override
    public String id() {
        return id;
    }

    @JsonCreator
    public static ProjectId valueOf(String uuid) {
        return new ProjectId(uuid);
    }
}
