package ru.practicum.shareit.request;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.User;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class ItemRequestMapper {

    public ItemRequestDto toDto(ItemRequest request) {
        if (request == null) {
            return null;
        }

        ItemRequestDto dto = new ItemRequestDto();
        dto.setId(request.getId());
        dto.setDescription(request.getDescription());
        dto.setCreated(request.getCreated());
        dto.setRequestorId(request.getRequestor() != null ? request.getRequestor().getId() : null);
        return dto;
    }

    public ItemRequestDto toDtoWithItems(ItemRequest request, List<ItemDto> items) {
        ItemRequestDto dto = toDto(request);
        if (dto != null) {
            dto.setItems(items != null ? items : List.of());
        }
        return dto;
    }

    public ItemRequest toEntity(ItemRequestDto dto, User requestor) {
        if (dto == null) {
            return null;
        }

        ItemRequest request = new ItemRequest();
        request.setId(dto.getId());
        request.setDescription(dto.getDescription());
        request.setRequestor(requestor);
        request.setCreated(dto.getCreated() != null ? dto.getCreated() : LocalDateTime.now());
        return request;
    }
}