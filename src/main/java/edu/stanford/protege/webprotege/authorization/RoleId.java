package edu.stanford.protege.webprotege.authorization;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import javax.annotation.Nonnull;

import java.util.Objects;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 4 Jan 2017
 */
public record RoleId(String id) {

    @JsonCreator
    public static RoleId valueOf(String id) {
        return new RoleId(id);
    }

    @JsonValue
    @Override
    public String id() {
        return id;
    }
}