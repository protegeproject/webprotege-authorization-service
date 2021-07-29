package edu.stanford.protege.webprotege.authorization;


import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 7 Jan 2017
 *
 * A persistence structure for role assignments.  This assumes the persistence is provided by
 * MongoDb, which is access via Morphia.
 */
@Document(collection = "RoleAssignments")
@CompoundIndexes({
        @CompoundIndex(def = "{'userName':1, 'projectId':1}", unique = true)
})
public class RoleAssignment {

    public static final String USER_NAME = "userName";

    public static final String PROJECT_ID = "projectId";

    public static final String ACTION_CLOSURE = "actionClosure";

    public static final String ROLE_CLOSURE = "roleClosure";

    @Nullable
    @SuppressWarnings("unused")
    private ObjectId id;

    @Nullable
    private String userName;

    @Nullable
    private String projectId;

    private List<String> assignedRoles = List.of();

    private List<String> roleClosure = List.of();

    private List<String> actionClosure = List.of();


    private RoleAssignment() {
    }

    public RoleAssignment(@Nullable String userName,
                          @Nullable String projectId,
                          @Nonnull List<String> assignedRoles,
                          @Nonnull List<String> roleClosure,
                          @Nonnull List<String> actionClosure) {
        this.userName = userName;
        this.projectId = projectId;
        this.assignedRoles = List.copyOf(Objects.requireNonNull(assignedRoles));
        this.roleClosure = List.copyOf(Objects.requireNonNull(roleClosure));
        this.actionClosure = List.copyOf(Objects.requireNonNull(actionClosure));
    }

    @Nonnull
    public Optional<String> getProjectId() {
        return Optional.ofNullable(projectId);
    }

    @Nonnull
    public Optional<String> getUserName() {
        return Optional.ofNullable(userName);
    }

    @Nonnull
    public List<String> getAssignedRoles() {
        return List.copyOf(assignedRoles);
    }

    @Nonnull
    public List<String> getRoleClosure() {
        return List.copyOf(roleClosure);
    }

    @Nonnull
    public List<String> getActionClosure() {
        return List.copyOf(actionClosure);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName, projectId, assignedRoles, roleClosure, actionClosure);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof RoleAssignment other)) {
            return false;
        }
        return Objects.equals(userName, other.userName)
                && Objects.equals(projectId, other.projectId)
                && this.assignedRoles.equals(other.assignedRoles)
                && this.roleClosure.equals(other.roleClosure)
                && this.actionClosure.equals(other.actionClosure);
    }

    @Override
    public String toString() {
        return "RoleAssignment{" + "id=" + id + ", userName='" + userName + '\'' + ", projectId='" + projectId + '\'' + ", assignedRoles=" + assignedRoles + ", roleClosure=" + roleClosure + ", actionClosure=" + actionClosure + '}';
    }
}
