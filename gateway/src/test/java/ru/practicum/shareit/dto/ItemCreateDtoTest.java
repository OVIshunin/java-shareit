package ru.practicum.shareit.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.item.dto.ItemCreateDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemCreateDtoTest {

    @Autowired
    private JacksonTester<ItemCreateDto> jacksonTester;

    @Test
    void testSerialize() throws Exception {
        ItemCreateDto dto = new ItemCreateDto("Дрель", "Мощная дрель", true, 1L);

        var json = jacksonTester.write(dto);

        assertThat(json).hasJsonPathValue("@.name");
        assertThat(json).hasJsonPathValue("@.description");
        assertThat(json).hasJsonPathValue("@.available");
        assertThat(json).hasJsonPathValue("@.requestId");
        assertThat(json).extractingJsonPathStringValue("@.name").isEqualTo("Дрель");
        assertThat(json).extractingJsonPathStringValue("@.description").isEqualTo("Мощная дрель");
        assertThat(json).extractingJsonPathBooleanValue("@.available").isTrue();
        assertThat(json).extractingJsonPathNumberValue("@.requestId").isEqualTo(1);
    }

    @Test
    void testDeserialize() throws Exception {
        String json = "{\"name\":\"Дрель\",\"description\":\"Мощная дрель\",\"available\":true,\"requestId\":1}";

        var dto = jacksonTester.parse(json);

        assertThat(dto.getObject().getName()).isEqualTo("Дрель");
        assertThat(dto.getObject().getDescription()).isEqualTo("Мощная дрель");
        assertThat(dto.getObject().getAvailable()).isTrue();
        assertThat(dto.getObject().getRequestId()).isEqualTo(1L);
    }

    @Test
    void testDeserializeWithoutRequestId() throws Exception {
        String json = "{\"name\":\"Дрель\",\"description\":\"Мощная дрель\",\"available\":true}";

        var dto = jacksonTester.parse(json);

        assertThat(dto.getObject().getRequestId()).isNull();
    }
}