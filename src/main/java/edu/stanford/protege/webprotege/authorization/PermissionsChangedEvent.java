package edu.stanford.protege.webprotege.authorization;

import com.fasterxml.jackson.annotation.JsonTypeName;
import edu.stanford.protege.webprotege.common.EventId;
import edu.stanford.protege.webprotege.common.ProjectEvent;
import edu.stanford.protege.webprotege.common.ProjectId;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 03/04/2013
 *
 * An event that is fired when the permissions for a project change.
 */
@JsonTypeName("webprotege.events.projects.PermissionsChanged")
public record PermissionsChangedEvent(EventId eventId,
                                      ProjectId projectId) implements ProjectEvent {

    public static final String CHANNEL = "webprotege.events.projects.PermissionsChanged";

    @Override
    public String getChannel() {
        return CHANNEL;
    }

    public PermissionsChangedEvent(EventId eventId, ProjectId projectId) {
        this.eventId = checkNotNull(eventId);
        this.projectId = checkNotNull(projectId);
    }
}
