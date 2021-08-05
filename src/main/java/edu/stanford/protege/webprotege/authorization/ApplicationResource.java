package edu.stanford.protege.webprotege.authorization;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.model.ProjectId;

import java.util.Optional;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 5 Jan 2017
 */
@JsonTypeName("Application")
public final class ApplicationResource implements Resource {

    private static final ApplicationResource INSTANCE = new ApplicationResource();

    private ApplicationResource() {

    }

    @JsonCreator
    public static ApplicationResource get() {
        return INSTANCE;
    }

    @Override
    public Optional<ProjectId> getProjectId() {
        return Optional.empty();
    }

    @Override
    public boolean isApplication() {
        return true;
    }

    @Override
    public boolean isProject() {
        return false;
    }

    @Override
    public boolean isProject(ProjectId projectId) {
        return false;
    }

    @Override
    public int hashCode() {
        return 22;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        return obj instanceof ApplicationResource;
    }

    @Override
    public String toString() {
        return "ApplicationResource{}";
    }
}

