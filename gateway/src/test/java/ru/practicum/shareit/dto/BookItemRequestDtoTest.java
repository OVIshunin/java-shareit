package ru.practicum.shareit.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.client.booking.dto.BookItemRequestDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookItemRequestDtoTest {

    @Autowired
    private JacksonTester<BookItemRequestDto> jacksonTester;

    @Test
    void testSerialize() throws Exception {
        LocalDateTime start = LocalDateTime.of(2025, 12, 25, 10, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 12, 27, 10, 0, 0);
        BookItemRequestDto dto = new BookItemRequestDto(1L, start, end);

        var json = jacksonTester.write(dto);

        assertThat(json).hasJsonPathValue("@.itemId");
        assertThat(json).hasJsonPathValue("@.start");
        assertThat(json).hasJsonPathValue("@.end");
        assertThat(json).extractingJsonPathNumberValue("@.itemId").isEqualTo(1);
    }

    @Test
    void testDeserialize() throws Exception {
        String json = "{\"itemId\":1,\"start\":\"2025-12-25T10:00:00\",\"end\":\"2025-12-27T10:00:00\"}";

        var dto = jacksonTester.parse(json);

        assertThat(dto.getObject().getItemId()).isEqualTo(1L);
        assertThat(dto.getObject().getStart()).isEqualTo(LocalDateTime.of(2025, 12, 25, 10, 0, 0));
        assertThat(dto.getObject().getEnd()).isEqualTo(LocalDateTime.of(2025, 12, 27, 10, 0, 0));
    }
}
