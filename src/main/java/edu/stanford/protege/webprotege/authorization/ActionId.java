package edu.stanford.protege.webprotege.authorization;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import javax.annotation.Nonnull;
import java.util.Comparator;
import static java.util.Objects.requireNonNull;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 4 Jan 2017
 */
public record ActionId(String id) implements Comparator<ActionId> {

    public ActionId(@Nonnull String id) {
        this.id = requireNonNull(id);
    }

    @JsonCreator
    public static ActionId valueOf(String id) {
        return new ActionId(id);
    }

    @JsonValue
    @Override
    public String id() {
        return id;
    }

    @Override
    public int compare(ActionId o1, ActionId o2) {
        return o1.id.compareTo(o2.id);
    }
}