package edu.stanford.protege.webprotege.authorization;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2021-07-28
 */
public record UserId(String id) {

    public static final String GUEST_USERNAME = "guest";

    @JsonValue
    @Override
    public String id() {
        return id;
    }

    public boolean isGuest() {
        return id.equals(GUEST_USERNAME);
    }

    public static UserId getGuest() {
        return new UserId(GUEST_USERNAME);
    }

    @JsonCreator
    public static UserId valueOf(String id) {
        if(id == null) {
            return getGuest();
        }
        return new UserId(id);
    }
}
