package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ItemRequestMapperTest {

    private final ItemRequestMapper mapper = new ItemRequestMapper();

    @Test
    void toDto_shouldConvertItemRequestToDto() {
        User requestor = new User(1L, "Requestor", "requestor@example.com");
        LocalDateTime created = LocalDateTime.now();
        ItemRequest request = new ItemRequest(1L, "Нужна дрель", requestor, created);

        ItemRequestDto result = mapper.toDto(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDescription()).isEqualTo("Нужна дрель");
        assertThat(result.getCreated()).isEqualTo(created);
        assertThat(result.getRequestorId()).isEqualTo(1L);
        assertThat(result.getItems()).isNull();
    }

    @Test
    void toDto_shouldReturnNull_whenRequestIsNull() {
        ItemRequestDto result = mapper.toDto(null);

        assertThat(result).isNull();
    }

    @Test
    void toDtoWithItems_shouldIncludeItems() {
        User requestor = new User(1L, "Requestor", "requestor@example.com");
        LocalDateTime created = LocalDateTime.now();
        ItemRequest request = new ItemRequest(1L, "Нужна дрель", requestor, created);

        ItemDto itemDto = new ItemDto(1L, "Дрель", "Мощная дрель", true, 1L);

        ItemRequestDto result = mapper.toDtoWithItems(request, List.of(itemDto));

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getName()).isEqualTo("Дрель");
    }

    @Test
    void toDtoWithItems_shouldSetEmptyList_whenItemsIsNull() {
        User requestor = new User(1L, "Requestor", "requestor@example.com");
        LocalDateTime created = LocalDateTime.now();
        ItemRequest request = new ItemRequest(1L, "Нужна дрель", requestor, created);

        ItemRequestDto result = mapper.toDtoWithItems(request, null);

        assertThat(result.getItems()).isEmpty();
    }

    @Test
    void toEntity_shouldConvertDtoToEntity() {
        User requestor = new User(1L, "Requestor", "requestor@example.com");
        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(1L);
        dto.setDescription("Нужна дрель");

        ItemRequest result = mapper.toEntity(dto, requestor);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDescription()).isEqualTo("Нужна дрель");
        assertThat(result.getRequestor()).isEqualTo(requestor);
        assertThat(result.getCreated()).isNotNull();
    }

    @Test
    void toEntity_shouldReturnNull_whenDtoIsNull() {
        ItemRequest result = mapper.toEntity(null, null);

        assertThat(result).isNull();
    }

    @Test
    void toEntity_shouldUseProvidedCreated_whenExists() {
        User requestor = new User(1L, "Requestor", "requestor@example.com");
        LocalDateTime created = LocalDateTime.of(2025, 1, 1, 12, 0);
        ItemRequestDto dto = new ItemRequestDto();
        dto.setCreated(created);
        dto.setDescription("Нужна дрель");

        ItemRequest result = mapper.toEntity(dto, requestor);

        assertThat(result.getCreated()).isEqualTo(created);
    }
}