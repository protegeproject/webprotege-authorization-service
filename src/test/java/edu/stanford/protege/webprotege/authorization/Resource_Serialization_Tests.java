package edu.stanford.protege.webprotege.authorization;

import edu.stanford.protege.webprotege.model.ProjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 2021-07-30
 */
@JsonTest
public class Resource_Serialization_Tests {

    @Autowired
    private JacksonTester<Resource> tester;

    @Test
    void shouldSerializeProjectResource() throws IOException {
        var projectResource = new ProjectResource(new ProjectId("11111111-1111-1111-1111-111111111111"));
        var written = tester.write(projectResource);
        System.out.println(written.getJson());
    }
}
