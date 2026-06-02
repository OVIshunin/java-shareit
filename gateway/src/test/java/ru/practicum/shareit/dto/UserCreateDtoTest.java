package ru.practicum.shareit.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.user.dto.UserCreateDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserCreateDtoTest {

    @Autowired
    private JacksonTester<UserCreateDto> jacksonTester;

    @Test
    void testSerialize() throws Exception {
        UserCreateDto dto = new UserCreateDto("John Doe", "john@example.com");

        var json = jacksonTester.write(dto);

        assertThat(json).hasJsonPathValue("@.name");
        assertThat(json).hasJsonPathValue("@.email");
        assertThat(json).extractingJsonPathStringValue("@.name").isEqualTo("John Doe");
        assertThat(json).extractingJsonPathStringValue("@.email").isEqualTo("john@example.com");
    }

    @Test
    void testDeserialize() throws Exception {
        String json = "{\"name\":\"Jane Doe\",\"email\":\"jane@example.com\"}";

        var dto = jacksonTester.parse(json);

        assertThat(dto.getObject().getName()).isEqualTo("Jane Doe");
        assertThat(dto.getObject().getEmail()).isEqualTo("jane@example.com");
    }
}