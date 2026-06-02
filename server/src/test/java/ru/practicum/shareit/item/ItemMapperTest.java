package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ItemMapperTest {

    private final ItemMapper itemMapper = new ItemMapper();

    @Test
    void toItemDto_shouldConvertItemToItemDto() {
        User owner = new User(1L, "Owner", "owner@example.com");
        Item item = new Item(1L, "Дрель", "Мощная дрель", true, owner, null);

        ItemDto result = itemMapper.toItemDto(item);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Дрель");
        assertThat(result.getDescription()).isEqualTo("Мощная дрель");
        assertThat(result.getAvailable()).isTrue();
        assertThat(result.getRequestId()).isNull();
    }

    @Test
    void toItemDto_shouldReturnNull_whenItemIsNull() {
        ItemDto result = itemMapper.toItemDto(null);

        assertThat(result).isNull();
    }

    @Test
    void toItem_shouldConvertItemDtoToItem() {
        User owner = new User(1L, "Owner", "owner@example.com");
        ItemDto itemDto = new ItemDto(1L, "Дрель", "Мощная дрель", true, null);

        Item result = itemMapper.toItem(itemDto, owner);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Дрель");
        assertThat(result.getDescription()).isEqualTo("Мощная дрель");
        assertThat(result.getAvailable()).isTrue();
        assertThat(result.getOwner()).isEqualTo(owner);
        assertThat(result.getRequest()).isNull();
    }

    @Test
    void toItem_shouldReturnNull_whenItemDtoIsNull() {
        User owner = new User(1L, "Owner", "owner@example.com");
        Item result = itemMapper.toItem(null, owner);

        assertThat(result).isNull();
    }

    @Test
    void toItemWithBookingsDto_shouldConvertItemToItemWithBookingsDto() {
        User owner = new User(1L, "Owner", "owner@example.com");
        Item item = new Item(1L, "Дрель", "Мощная дрель", true, owner, null);
        BookingShortDto lastBooking = new BookingShortDto(1L, 2L);
        BookingShortDto nextBooking = new BookingShortDto(2L, 2L);
        CommentDto comment = new CommentDto();
        comment.setId(1L);
        comment.setText("Отличная вещь!");
        comment.setAuthorName("Booker");
        comment.setCreated(LocalDateTime.now());

        ItemWithBookingsDto result = itemMapper.toItemWithBookingsDto(
                item, lastBooking, nextBooking, List.of(comment));

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Дрель");
        assertThat(result.getDescription()).isEqualTo("Мощная дрель");
        assertThat(result.getAvailable()).isTrue();
        assertThat(result.getLastBooking()).isEqualTo(lastBooking);
        assertThat(result.getNextBooking()).isEqualTo(nextBooking);
        assertThat(result.getComments()).hasSize(1);
    }

    @Test
    void toItemWithBookingsDto_shouldReturnNull_whenItemIsNull() {
        ItemWithBookingsDto result = itemMapper.toItemWithBookingsDto(null, null, null, null);

        assertThat(result).isNull();
    }

    @Test
    void toItemWithBookingsDto_shouldSetEmptyComments_whenCommentsIsNull() {
        User owner = new User(1L, "Owner", "owner@example.com");
        Item item = new Item(1L, "Дрель", "Мощная дрель", true, owner, null);

        ItemWithBookingsDto result = itemMapper.toItemWithBookingsDto(item, null, null, null);

        assertThat(result.getComments()).isEmpty();
    }

    @Test
    void toCommentDto_shouldConvertCommentToCommentDto() {
        // given
        LocalDateTime created = LocalDateTime.now();
        Comment comment = new Comment(1L, "Отличная вещь!", 1L, 2L, created);
        String authorName = "John Doe";

        // when
        CommentDto result = itemMapper.toCommentDto(comment, authorName);

        // then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getText()).isEqualTo("Отличная вещь!");
        assertThat(result.getAuthorName()).isEqualTo("John Doe");
        assertThat(result.getCreated()).isEqualTo(created);
    }

    @Test
    void toCommentDto_shouldReturnNull_whenCommentIsNull() {
        // when
        CommentDto result = itemMapper.toCommentDto(null, "John Doe");

        // then
        assertThat(result).isNull();
    }

    @Test
    void toComment_shouldConvertCommentDtoToComment() {
        // given
        CommentDto commentDto = new CommentDto();
        commentDto.setText("Отличная вещь!");
        Long itemId = 1L;
        Long authorId = 2L;

        // when
        Comment result = itemMapper.toComment(commentDto, itemId, authorId);

        // then
        assertThat(result.getId()).isNull();
        assertThat(result.getText()).isEqualTo("Отличная вещь!");
        assertThat(result.getItemId()).isEqualTo(1L);
        assertThat(result.getAuthorId()).isEqualTo(2L);
        assertThat(result.getCreated()).isNotNull();
    }

    @Test
    void toComment_shouldReturnNull_whenCommentDtoIsNull() {
        // when
        Comment result = itemMapper.toComment(null, 1L, 2L);

        // then
        assertThat(result).isNull();
    }
}