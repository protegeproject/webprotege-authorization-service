package edu.stanford.protege.webprotege.authorization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

class RoleId_Tests {


    private RoleId roleId;

    private String id = "The id";

    @BeforeEach
    public void setUp() {
        roleId = new RoleId(id);
    }

    @SuppressWarnings("ConstantConditions")
    public void shouldThrowNullPointerExceptionIf_id_IsNull() {
        assertThrows(NullPointerException.class, () -> {
            new RoleId(null);
        });
    }

    @Test
    public void shouldReturnSupplied_id() {
        assertThat(roleId.id(), is(this.id));
    }

    @Test
    public void shouldBeEqualToSelf() {
        assertThat(roleId, is(roleId));
    }

    @Test
    @SuppressWarnings("ObjectEqualsNull")
    public void shouldNotBeEqualToNull() {
        assertThat(roleId.equals(null), is(false));
    }

    @Test
    public void shouldBeEqualToOther() {
        assertThat(roleId, is(new RoleId(id)));
    }

    @Test
    public void shouldNotBeEqualToOtherThatHasDifferent_id() {
        assertThat(roleId, is(not(new RoleId("String-fefb9f3e-0859-40e3-89d5-06ac997b1794"))));
    }

    @Test
    public void shouldBeEqualToOtherHashCode() {
        assertThat(roleId.hashCode(), is(new RoleId(id).hashCode()));
    }

    @Test
    public void shouldImplementToString() {
        assertThat(roleId.toString(), startsWith("RoleId"));
    }

}