package edu.stanford.protege.webprotege.authorization;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2021-07-29
 */
@JsonTest
public class RoleId_Serialization_Tests {


    public static final String ID = "ProjectEditor";

    @Autowired
    private JacksonTester<RoleId> tester;

    @Test
    void shouldSerializeToJson() throws IOException {
        var roleId = new RoleId(ID);
        var written = tester.write(roleId);
        var json = written.getJson();
        assertThat(json).isEqualTo("\"" + ID + "\"");
    }

    @Test
    void shouldDeserializeFromJson() throws IOException {
        var json = "\"" + ID + "\"";
        var parsed = tester.parse(json);
        var roleId = parsed.getObject();
        assertThat(roleId.id()).isEqualTo(ID);
    }
}
