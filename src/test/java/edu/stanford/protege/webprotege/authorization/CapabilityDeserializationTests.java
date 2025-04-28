package edu.stanford.protege.webprotege.authorization;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class CapabilityDeserializationTests {

        private JacksonTester<Capability> tester;

        @BeforeEach
        void setUp() {
            JacksonTester.initFields(this, new ObjectMapper());
        }

        @Test
        void shouldDeserializeAndSerialize() throws IOException {
            var json = """
                {
                    "@type" : "OtherTypeNotRecognized",
                    "id" : "PerformActionX",
                    "otherProperty" : "OtherValue"
                }
                """;
            var parsed = tester.parse(json);
            assertThat(parsed).isEqualTo(new GenericParameterizedCapability("OtherTypeNotRecognized", "PerformActionX", Map.of("otherProperty", "OtherValue")));

            var written = tester.write(parsed.getObject());
            assertThat(written).extractingJsonPathStringValue("['@type']").isEqualTo("OtherTypeNotRecognized");
            assertThat(written).extractingJsonPathStringValue("['id']").isEqualTo("PerformActionX");
            assertThat(written).extractingJsonPathStringValue("['otherProperty']").isEqualTo("OtherValue");
        }

}
