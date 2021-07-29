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
public class ProjectId_Serialization_Tests {

    public static final String ID = "12345678-1234-1234-1234-123456789abc";

    @Autowired
    private JacksonTester<ProjectId> tester;

    @Test
    void shouldSerializeToJson() throws IOException {
        var projectId = new ProjectId(ID);
        var written = tester.write(projectId);
        var json = written.getJson();
        assertThat(json).isEqualTo("\"" + ID + "\"");
    }

    @Test
    void shouldDeserializeFromJson() throws IOException {
        var json = "\"" + ID + "\"";
        var parsed = tester.parse(json);
        var projectId = parsed.getObject();
        assertThat(projectId.id()).isEqualTo(ID);
    }
}
