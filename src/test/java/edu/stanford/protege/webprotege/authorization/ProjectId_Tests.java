package edu.stanford.protege.webprotege.authorization;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("ALL")
class ProjectId_Tests {


    @Test
    public void equalsShouldReturnTrueForObjectsWithSameId() {
        var uuid = "0d8f03d4-d9bb-496d-a78c-146868af8265";
        var projectIdA = ProjectId.valueOf(uuid);
        var projectIdB = ProjectId.valueOf(uuid);
        assertEquals(projectIdA, projectIdB);
    }

    @Test
    public void equalsShouldReturnFalseForObjectsWithDifferentIds() {
        var uuidA = "0d8f03d4-d9bb-496d-a78c-146868af8265";
        var uuidB = "d16a6ca0-3afc-4af3-8a95-82cdc82f52cc";
        var projectIdA = ProjectId.valueOf(uuidA);
        var projectIdB = ProjectId.valueOf(uuidB);
        assertNotEquals(projectIdA, projectIdB);
    }

    @Test
    public void equalsNullReturnsFalse() {
        var uuid = "0d8f03d4-d9bb-496d-a78c-146868af8265";
        var projectId = ProjectId.valueOf(uuid);
        assertNotNull(projectId);
    }

    public void malformedUUIDThrowsProjectIdFormatException() {
        assertThrows(IllegalArgumentException.class, () -> {
           ProjectId.valueOf("wrong");
        });
    }

    @Test
    public void getIdReturnsSuppliedValue() {
        var uuid = "0d8f03d4-d9bb-496d-a78c-146868af8265";
        var projectId = ProjectId.valueOf(uuid);
        assertEquals(uuid, projectId.id());
    }

    public void getThrowsNullPointerForNullArgument() {
        assertThrows(NullPointerException.class, () -> {
            ProjectId.valueOf(null);
        });
    }

}