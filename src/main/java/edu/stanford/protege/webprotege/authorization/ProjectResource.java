package edu.stanford.protege.webprotege.authorization;

import java.util.Objects;
import java.util.Optional;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 5 Jan 2017
 */
public final class ProjectResource implements Resource {

    private final ProjectId projectId;

    public ProjectResource(ProjectId projectId) {
        this.projectId = Objects.requireNonNull(projectId);
    }

    public static ProjectResource forProject(ProjectId projectId) {
        return new ProjectResource(projectId);
    }

    @Override
    public Optional<ProjectId> getProjectId() {
        return Optional.of(projectId);
    }

    @Override
    public boolean isProject(ProjectId projectId) {
        return this.projectId.equals(projectId);
    }

    @Override
    public boolean isProject() {
        return true;
    }

    @Override
    public boolean isApplication() {
        return false;
    }

    @Override
    public int hashCode() {
        return projectId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ProjectResource other)) {
            return false;
        }
        return this.projectId.equals(other.projectId);
    }

    @Override
    public String toString() {
        return "ProjectResource{" + "projectId=" + projectId + '}';
    }
}
