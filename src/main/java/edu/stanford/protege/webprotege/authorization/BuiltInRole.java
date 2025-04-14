package edu.stanford.protege.webprotege.authorization;

import com.google.common.base.CaseFormat;

import java.util.*;
import java.util.stream.Collectors;

import static edu.stanford.protege.webprotege.authorization.BuiltInCapability.*;
import static edu.stanford.protege.webprotege.authorization.RoleType.APPLICATION_ROLE;
import static edu.stanford.protege.webprotege.authorization.RoleType.PROJECT_ROLE;
import static java.util.function.Predicate.not;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 5 Jan 2017
 */
public enum BuiltInRole {

    // Application Roles

    PROJECT_CREATOR(APPLICATION_ROLE, CREATE_EMPTY_PROJECT),

    PROJECT_UPLOADER(APPLICATION_ROLE, UPLOAD_PROJECT),

    ACCOUNT_CREATOR(APPLICATION_ROLE, CREATE_ACCOUNT),

    USER_ADMIN(APPLICATION_ROLE,
               ACCOUNT_CREATOR,
               VIEW_ANY_USER_DETAILS,
               DELETE_ANY_ACCOUNT,
               RESET_ANY_USER_PASSWORD),

    SYSTEM_ADMIN(APPLICATION_ROLE,
            USER_ADMIN,
                 MOVE_ANY_PROJECT_TO_TRASH,
                 SUBSTITUTE_USER,
                 EDIT_APPLICATION_SETTINGS,
                 REBUILD_PERMISSIONS),


    // Project Roles

    PROJECT_DOWNLOADER(PROJECT_ROLE,
            DOWNLOAD_PROJECT),



    ISSUE_VIEWER(PROJECT_ROLE, VIEW_ANY_ISSUE),

    ISSUE_COMMENTER(PROJECT_ROLE,
            ISSUE_VIEWER,
                    COMMENT_ON_ISSUE),

    ISSUE_CREATOR(PROJECT_ROLE, ISSUE_COMMENTER,
                  CREATE_ISSUE,
                  ASSIGN_OWN_ISSUE_TO_SELF,
                  CLOSE_OWN_ISSUE),

    ISSUE_MANAGER(PROJECT_ROLE, ISSUE_CREATOR,
                  ASSIGN_ANY_ISSUE_TO_ANYONE,
                  CLOSE_ANY_ISSUE,
                  UPDATE_ANY_ISSUE_TITLE,
                  UPDATE_ANY_ISSUE_BODY),



    PROJECT_VIEWER(PROJECT_ROLE, VIEW_PROJECT,
                   VIEW_OBJECT_COMMENT,
                   EDIT_OWN_OBJECT_COMMENT,
                   ADD_OR_REMOVE_VIEW,
                   ADD_OR_REMOVE_PERSPECTIVE,
                   VIEW_CHANGES,
                   WATCH_CHANGES),

    OBJECT_COMMENTER(PROJECT_ROLE, PROJECT_VIEWER,
                     CREATE_OBJECT_COMMENT,
                     EDIT_OWN_OBJECT_COMMENT,
                     SET_OBJECT_COMMENT_STATUS,
                     EDIT_ENTITY_TAGS),

    PROJECT_EDITOR(PROJECT_ROLE, OBJECT_COMMENTER,
                   EDIT_ONTOLOGY,
                   EDIT_ONTOLOGY_ANNOTATIONS,
                   CREATE_CLASS,
                   DELETE_CLASS,
                   MERGE_ENTITIES,
                   CREATE_PROPERTY,
                   DELETE_PROPERTY,
                   CREATE_INDIVIDUAL,
                   DELETE_INDIVIDUAL,
                   CREATE_DATATYPE,
                   DELETE_DATATYPE,
                   REVERT_CHANGES),

    LAYOUT_EDITOR(PROJECT_ROLE, ADD_OR_REMOVE_PERSPECTIVE,
                  ADD_OR_REMOVE_VIEW),

    PROJECT_MANAGER(PROJECT_ROLE, PROJECT_EDITOR,
                    LAYOUT_EDITOR,
                    SAVE_DEFAULT_PROJECT_LAYOUT,
                    EDIT_PROJECT_SETTINGS,
                    EDIT_DEFAULT_VISUALIZATION_SETTINGS,
                    EDIT_SHARING_SETTINGS,
                    EDIT_NEW_ENTITY_SETTINGS,
                    EDIT_PROJECT_PREFIXES,
                    UPLOAD_AND_MERGE,
                    EDIT_PROJECT_TAGS,
                    EDIT_FORMS,
                    UPLOAD_AND_MERGE_ADDITIONS,
                    EDIT_PROJECT_TAGS),


    // Roles that relate to the UI

    CAN_VIEW(PROJECT_ROLE, PROJECT_VIEWER, ISSUE_VIEWER, PROJECT_DOWNLOADER),

    CAN_COMMENT(PROJECT_ROLE, CAN_VIEW, ISSUE_CREATOR, OBJECT_COMMENTER),

    CAN_EDIT(PROJECT_ROLE, PROJECT_EDITOR, CAN_COMMENT),

    CAN_MANAGE(PROJECT_ROLE, CAN_EDIT, PROJECT_MANAGER, ISSUE_MANAGER);


    private static final Set<RoleId> builtInApplicationRoles = new HashSet<>();

    static {
        Arrays.stream(values())
                .filter(BuiltInRole::isBuiltInApplicationRole)
                .map(BuiltInRole::getRoleId)
                .forEach(builtInApplicationRoles::add);

        performSanityCheck();
    }

    public static boolean isBuiltInApplicationRole(RoleId roleId) {
        return builtInApplicationRoles.contains(roleId);
    }


    private final RoleId roleId;

    private final RoleType roleType;

    private final List<BuiltInRole> parents;

    private final List<BuiltInCapability> capabilities;



    BuiltInRole(RoleType roleType, List<BuiltInRole> parents, List<BuiltInCapability> capabilities) {
        this.roleId = new RoleId(CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, name()));
        this.roleType = roleType;
        this.parents = List.copyOf(parents);
        this.capabilities = List.copyOf(capabilities);
    }

    BuiltInRole(RoleType roleType, BuiltInCapability... capabilities) {
        this(roleType, List.of(), List.copyOf(Arrays.asList(capabilities)));
    }


    BuiltInRole(RoleType roleType, BuiltInRole parentRole, BuiltInCapability... capabilities) {
        this(roleType, List.of(parentRole), List.copyOf(Arrays.asList(capabilities)));
    }

    BuiltInRole(RoleType roleType, BuiltInRole parentRole1, BuiltInRole parentRole2, BuiltInCapability... capabilities) {
        this(roleType, List.of(parentRole1, parentRole2), List.copyOf(Arrays.asList(capabilities)));
    }

    BuiltInRole(RoleType roleType, BuiltInRole parentRole1, BuiltInRole parentRole2, BuiltInRole parentRole3, BuiltInCapability... capabilities) {
        this(roleType, List.of(parentRole1, parentRole2, parentRole3), List.copyOf(Arrays.asList(capabilities)));
    }

    public RoleId getRoleId() {
        return roleId;
    }

    public List<BuiltInRole> getParents() {
        return parents;
    }

    public List<Capability> getCapabilities() {
        return capabilities.stream()
                .map(BuiltInCapability::getCapability)
                .toList();
    }

    public boolean isProjectRole() {
        return roleType.equals(PROJECT_ROLE);
    }

    public boolean isBuiltInApplicationRole() {
        return roleType.equals(APPLICATION_ROLE);
    }

    public RoleType getRoleType() {
        return roleType;
    }


    public RoleDefinition toRoleDefinition() {
        return RoleDefinition.get(this.getRoleId(), this.getRoleType(), this.getParents().stream().map(BuiltInRole::getRoleId).collect(Collectors.toSet()), new LinkedHashSet<>(this.getCapabilities()), "Built in role");
    }

    /**
     * Gets the list of built-in roles that are project roles.
     * @return The list of built-in roles.
     */
    public List<BuiltInRole> getProjectRoles() {
        return Arrays.stream(values())
                .filter(BuiltInRole::isProjectRole)
                .toList();
    }

    public List<BuiltInRole> getApplicationRoles() {
        return Arrays.stream(values())
                .filter(not(BuiltInRole::isProjectRole))
                .toList();
    }

    private static void performSanityCheck() {
        for(BuiltInRole role : values()) {
            if(role.isBuiltInApplicationRole()) {
                for(BuiltInRole parentRole : role.getParents()) {
                    if(!parentRole.isBuiltInApplicationRole()) {
                        // WARNING
                    }
                }
            }
        }
    }
}
