package edu.stanford.protege.webprotege.authorization;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import edu.stanford.protege.webprotege.model.ProjectId;

import java.util.Optional;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 5 Jan 2017
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(ProjectResource.class),
        @JsonSubTypes.Type(ApplicationResource.class)
})
public interface Resource {

    Optional<ProjectId> getProjectId();

    boolean isApplication();

    boolean isProject();

    boolean isProject(ProjectId projectId);
}

