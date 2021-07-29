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
public class ActionId_Serialization_Tests {


    public static final String ID = "CreateClasses";

    @Autowired
    private JacksonTester<ActionId> tester;

    @Test
    void shouldSerializeToJson() throws IOException {
        var actionId = new ActionId(ID);
        var written = tester.write(actionId);
        var json = written.getJson();
        assertThat(json).isEqualTo("\"" + ID + "\"");
    }

    @Test
    void shouldDeserializeFromJson() throws IOException {
        var json = "\"" + ID + "\"";
        var parsed = tester.parse(json);
        var actionId = parsed.getObject();
        assertThat(actionId.id()).isEqualTo(ID);
    }
    
}
