package com.ailegorreta.testservice.domain;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * This is an example how we can test a DTO to be serializable in a JSON and deserialize
 */
@JsonTest
public class SomeDTOJsonTest {
    @Autowired
    private JacksonTester<SomeDTO> json;

    @Test
    void testSerialize() throws Exception {
        var now = Instant.now();
        var dto = new SomeDTO(394L, "1234567890", "Title", "Author", 9.90,
                        "Polarsophia");
        var jsonContent = json.write(dto);
        assertThat(jsonContent).extractingJsonPathNumberValue("@.id")
                .isEqualTo(dto.id().intValue());
        assertThat(jsonContent).extractingJsonPathStringValue("@.isbn")
                .isEqualTo(dto.isbn());
        assertThat(jsonContent).extractingJsonPathStringValue("@.title")
                .isEqualTo(dto.title());
        assertThat(jsonContent).extractingJsonPathStringValue("@.author")
                .isEqualTo(dto.author());
        assertThat(jsonContent).extractingJsonPathNumberValue("@.price")
                .isEqualTo(dto.price());
        assertThat(jsonContent).extractingJsonPathStringValue("@.publisher")
                .isEqualTo(dto.publisher());
    }

    @Test
    void testDeserialize() throws Exception {
        var instant = Instant.parse("2021-09-07T22:50:37.135029Z");
        var content = """
                {
                    "id": 394,
                    "isbn": "1234567890",
                    "title": "Title",
                    "author": "Author",
                    "price": 9.90,
                    "publisher": "Polarsophia"
                }
                """;
        assertThat(json.parse(content))
                .usingRecursiveComparison()
                .isEqualTo(new SomeDTO(394L, "1234567890", "Title", "Author", 9.90,
                        "Polarsophia"));
    }

}
