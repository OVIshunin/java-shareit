package ru.practicum.shareit.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.item.dto.CommentCreateDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class CommentCreateDtoTest {

    @Autowired
    private JacksonTester<CommentCreateDto> jacksonTester;

    @Test
    void testSerialize() throws Exception {
        CommentCreateDto dto = new CommentCreateDto("Отличная вещь!");

        var json = jacksonTester.write(dto);

        assertThat(json).hasJsonPathValue("@.text");
        assertThat(json).extractingJsonPathStringValue("@.text").isEqualTo("Отличная вещь!");
    }

    @Test
    void testDeserialize() throws Exception {
        String json = "{\"text\":\"Отличная вещь!\"}";

        var dto = jacksonTester.parse(json);

        assertThat(dto.getObject().getText()).isEqualTo("Отличная вещь!");
    }
}
