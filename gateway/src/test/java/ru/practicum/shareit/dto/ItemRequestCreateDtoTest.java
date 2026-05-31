package ru.practicum.shareit.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestCreateDtoTest {

    @Autowired
    private JacksonTester<ItemRequestCreateDto> jacksonTester;

    @Test
    void testSerialize() throws Exception {
        ItemRequestCreateDto dto = new ItemRequestCreateDto("Нужна дрель для ремонта");

        var json = jacksonTester.write(dto);

        assertThat(json).hasJsonPathValue("@.description");
        assertThat(json).extractingJsonPathStringValue("@.description").isEqualTo("Нужна дрель для ремонта");
    }

    @Test
    void testDeserialize() throws Exception {
        String json = "{\"description\":\"Нужна дрель для ремонта\"}";

        var dto = jacksonTester.parse(json);

        assertThat(dto.getObject().getDescription()).isEqualTo("Нужна дрель для ремонта");
    }
}