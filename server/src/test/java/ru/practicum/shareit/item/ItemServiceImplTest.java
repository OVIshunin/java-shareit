package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.comment.Comment;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemServiceImpl itemService;

    private User owner;
    private User booker;
    private Item item;
    private ItemDto itemDto;
    private Comment comment;
    private CommentDto commentDto;
    private Booking lastBooking;
    private Booking nextBooking;

    @BeforeEach
    void setUp() {
        owner = new User(1L, "Owner", "owner@example.com");
        booker = new User(2L, "Booker", "booker@example.com");

        item = new Item(1L, "Дрель", "Мощная дрель", true, owner, null);
        itemDto = new ItemDto(1L, "Дрель", "Мощная дрель", true, null);

        comment = new Comment(1L, "Отличная вещь!", 1L, 2L, LocalDateTime.now());
        commentDto = new CommentDto();
        commentDto.setText("Отличная вещь!");

        lastBooking = new Booking(1L, LocalDateTime.now().minusDays(5), LocalDateTime.now().minusDays(3), item, booker, BookingStatus.APPROVED);
        nextBooking = new Booking(2L, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(3), item, booker, BookingStatus.APPROVED);
    }

    // ========== getUserItems ==========

    @Test
    void getUserItems_shouldReturnListOfItemsWithBookingsAndComments() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findByOwnerOrderByIdAsc(owner)).thenReturn(List.of(item));
        when(bookingRepository.findLastApprovedBookingByItemId(eq(1L), any(LocalDateTime.class))).thenReturn(Optional.empty());
        when(bookingRepository.findNextApprovedBookingByItemId(eq(1L), any(LocalDateTime.class))).thenReturn(Optional.empty());
        when(itemMapper.toItemWithBookingsDto(eq(item), any(), any(), any())).thenReturn(new ItemWithBookingsDto());

        List<ItemWithBookingsDto> result = itemService.getUserItems(1L);

        assertThat(result).hasSize(1);
        verify(userRepository).findById(1L);
        verify(itemRepository).findByOwnerOrderByIdAsc(owner);
    }

    @Test
    void getUserItems_shouldThrowNotFoundException_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.getUserItems(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found with id: 99");
    }

    // ========== getItemById ==========

    @Test
    void getItemById_shouldReturnItemWithBookingsAndComments_whenUserIsOwner() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingRepository.findLastApprovedBookingByItemId(eq(1L), any(LocalDateTime.class))).thenReturn(Optional.of(lastBooking));
        when(bookingRepository.findNextApprovedBookingByItemId(eq(1L), any(LocalDateTime.class))).thenReturn(Optional.of(nextBooking));
        when(commentRepository.findByItemIdOrderByCreatedDesc(1L)).thenReturn(List.of(comment));
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemMapper.toItemWithBookingsDto(eq(item), any(), any(), any())).thenReturn(new ItemWithBookingsDto());

        ItemWithBookingsDto result = itemService.getItemById(1L, 1L);

        assertThat(result).isNotNull();
        verify(itemRepository).findById(1L);
    }

    @Test
    void getItemById_shouldThrowNotFoundException_whenItemNotFound() {
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.getItemById(99L, 1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Item not found with id: 99");
    }

    // ========== createItem ==========

    @Test
    void createItem_shouldCreateAndReturnItem() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemMapper.toItem(itemDto, owner)).thenReturn(item);
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        when(itemMapper.toItemDto(item)).thenReturn(itemDto);

        ItemDto result = itemService.createItem(1L, itemDto);

        assertThat(result.getName()).isEqualTo("Дрель");
        assertThat(result.getDescription()).isEqualTo("Мощная дрель");
        assertThat(result.getAvailable()).isTrue();

        verify(userRepository).findById(1L);
        verify(itemMapper).toItem(itemDto, owner);
        verify(itemRepository).save(any(Item.class));
        verify(itemMapper).toItemDto(item);
    }

    @Test
    void createItem_shouldThrowNotFoundException_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.createItem(99L, itemDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found with id: 99");
    }

    @Test
    void createItem_shouldThrowValidationException_whenNameIsBlank() {
        itemDto.setName("");
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));

        assertThatThrownBy(() -> itemService.createItem(1L, itemDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Item name cannot be empty");
    }

    @Test
    void createItem_shouldThrowValidationException_whenDescriptionIsBlank() {
        itemDto.setDescription("");
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));

        assertThatThrownBy(() -> itemService.createItem(1L, itemDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Item description cannot be empty");
    }

    @Test
    void createItem_shouldThrowValidationException_whenAvailableIsNull() {
        itemDto.setAvailable(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));

        assertThatThrownBy(() -> itemService.createItem(1L, itemDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Item available status cannot be null");
    }

    // ========== updateItem ==========

    @Test
    void updateItem_shouldUpdateNameAndDescriptionAndAvailable() {
        ItemDto updateDto = new ItemDto(null, "Обновлённая дрель", "Ещё мощнее", false, null);
        Item updatedItem = new Item(1L, "Обновлённая дрель", "Ещё мощнее", false, owner, null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(updatedItem);
        when(itemMapper.toItemDto(updatedItem)).thenReturn(updateDto);

        ItemDto result = itemService.updateItem(1L, 1L, updateDto);

        assertThat(result.getName()).isEqualTo("Обновлённая дрель");
        assertThat(result.getDescription()).isEqualTo("Ещё мощнее");
        assertThat(result.getAvailable()).isFalse();

        verify(itemRepository).save(any(Item.class));
    }

    @Test
    void updateItem_shouldThrowNotFoundException_whenItemNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.updateItem(99L, 1L, itemDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Item not found with id: 99");
    }

    @Test
    void updateItem_shouldThrowAccessDeniedException_whenUserIsNotOwner() {
        when(userRepository.findById(99L)).thenReturn(Optional.of(owner));  // ← исправлено: 99L
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> itemService.updateItem(1L, 99L, itemDto))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("User is not the owner of this item");
    }

    // ========== search ==========

    @Test
    void search_shouldReturnListOfItems_whenTextIsNotEmpty() {
        when(itemRepository.search("дрель")).thenReturn(List.of(item));
        when(itemMapper.toItemDto(item)).thenReturn(itemDto);

        List<ItemDto> result = itemService.search("дрель");

        assertThat(result).hasSize(1);
        verify(itemRepository).search("дрель");
    }

    @Test
    void search_shouldReturnEmptyList_whenTextIsBlank() {
        List<ItemDto> result = itemService.search("");

        assertThat(result).isEmpty();
        verify(itemRepository, never()).search(any());
    }

    @Test
    void search_shouldReturnEmptyList_whenTextIsNull() {
        List<ItemDto> result = itemService.search(null);

        assertThat(result).isEmpty();
        verify(itemRepository, never()).search(any());
    }

    // ========== addComment ==========

    @Test
    void addComment_shouldCreateAndReturnComment() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.existsApprovedBookingByUserAndItemAndEndBefore(eq(2L), eq(1L), any(LocalDateTime.class))).thenReturn(true);
        when(itemMapper.toComment(commentDto, 1L, 2L)).thenReturn(comment);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        when(itemMapper.toCommentDto(comment, booker.getName())).thenReturn(commentDto);

        CommentDto result = itemService.addComment(1L, 2L, commentDto);

        assertThat(result.getText()).isEqualTo("Отличная вещь!");
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void addComment_shouldThrowNotFoundException_whenItemNotFound() {
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.addComment(99L, 2L, commentDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Item not found with id: 99");
    }

    @Test
    void addComment_shouldThrowNotFoundException_whenUserNotFound() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.addComment(1L, 99L, commentDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found with id: 99");
    }

    @Test
    void addComment_shouldThrowBadRequestException_whenUserHasNotBooked() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.existsApprovedBookingByUserAndItemAndEndBefore(eq(2L), eq(1L), any(LocalDateTime.class))).thenReturn(false);

        assertThatThrownBy(() -> itemService.addComment(1L, 2L, commentDto))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("User has not booked this item or booking is not completed");
    }

    @Test
    void addComment_shouldThrowValidationException_whenCommentTextIsBlank() {
        commentDto.setText("");
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.existsApprovedBookingByUserAndItemAndEndBefore(eq(2L), eq(1L), any(LocalDateTime.class))).thenReturn(true);

        assertThatThrownBy(() -> itemService.addComment(1L, 2L, commentDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Comment text cannot be empty");
    }
}