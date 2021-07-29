package edu.stanford.protege.webprotege.authorization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;

class UserId_Tests {



    public static final String THE_USER_NAME = "The User Name";
    private UserId userId;

    private UserId otherUserId;

    @BeforeEach
    void setUp() {
        userId = UserId.valueOf(THE_USER_NAME);
        otherUserId = UserId.valueOf(THE_USER_NAME);
    }

    @Test
    public void shouldReturnGuestUser() {
        assertThat(UserId.valueOf(null), is(UserId.getGuest()));
    }

    @Test
    public void shouldReturnTrue() {
        assertThat(UserId.getGuest().isGuest(), is(true));
    }

    @Test
    public void shouldReturnFalse() {
        assertThat(userId.isGuest(), is(false));
    }

    @Test
    public void shouldBeEqualToSelf() {
        assertThat(userId, is(equalTo(userId)));
    }

    @Test
    public void shouldNotBeEqualToNull() {
        assertThat(userId, is(not(equalTo(null))));
    }

    @Test
    public void shouldBeEqualToOther() {
        assertThat(userId, is(equalTo(otherUserId)));
    }

    @Test
    public void shouldHaveSameHashCodeAsOther() {
        assertThat(userId.hashCode(), is(otherUserId.hashCode()));
    }

    @Test
    public void shouldGenerateToString() {
        assertThat(userId.toString(), startsWith("UserId"));
    }

    @Test
    public void shouldReturnSuppliedUserName() {
        assertThat(userId.id(), is(THE_USER_NAME));
    }

}