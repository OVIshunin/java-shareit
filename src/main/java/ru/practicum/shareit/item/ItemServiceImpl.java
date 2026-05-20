package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemMapper itemMapper;

    @Override
    public List<ItemWithBookingsDto> getUserItems(Long userId) {
        log.info("Getting all items for user id: {}", userId);

        checkUserExists(userId);

        List<Item> items = itemRepository.findByOwnerOrderByIdAsc(userId);
        List<ItemWithBookingsDto> result = new ArrayList<>();

        for (Item item : items) {
            result.add(buildItemWithBookingsAndComments(item, userId));
        }

        return result;
    }

    @Override
    public ItemWithBookingsDto getItemById(Long id, Long userId) {
        log.info("Getting item by id: {} for user: {}", id, userId);

        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Item not found with id: " + id));

        return buildItemWithBookingsAndComments(item, userId);
    }

    @Override
    @Transactional
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        log.info("Creating item for user id: {}", userId);

        checkUserExists(userId);

        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new ValidationException("Item name cannot be empty");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            throw new ValidationException("Item description cannot be empty");
        }
        if (itemDto.getAvailable() == null) {
            throw new ValidationException("Item available status cannot be null");
        }

        Item item = itemMapper.toItem(itemDto, userId);
        item = itemRepository.save(item);
        log.info("Item created with id: {}", item.getId());

        return itemMapper.toItemDto(item);
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long itemId, Long userId, ItemDto itemDto) {
        log.info("Updating item id: {} by user: {}", itemId, userId);

        checkUserExists(userId);

        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found with id: " + itemId));

        if (!existingItem.getOwner().equals(userId)) {
            throw new AccessDeniedException("User is not the owner of this item");
        }

        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            existingItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            existingItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }

        existingItem = itemRepository.save(existingItem);
        log.info("Item updated successfully");

        return itemMapper.toItemDto(existingItem);
    }

    @Override
    public List<ItemDto> search(String text) {
        log.info("Searching items with text: {}", text);

        if (text == null || text.isBlank()) {
            return List.of();
        }

        return itemRepository.search(text).stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long itemId, Long userId, CommentDto commentDto) {
        log.info("Adding comment to item id: {} by user: {}", itemId, userId);

        // Проверка существования вещи
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found with id: " + itemId));

        // Проверка существования пользователя
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        // Проверка: пользователь должен арендовать вещь и аренда должна быть завершена
        boolean hasBooked = bookingRepository.existsApprovedBookingByUserAndItemAndEndBefore(
                userId, itemId, LocalDateTime.now());

        if (!hasBooked) {
            throw new BadRequestException("User has not booked this item or booking is not completed");
        }

        // Проверка текста комментария
        if (commentDto.getText() == null || commentDto.getText().isBlank()) {
            throw new ValidationException("Comment text cannot be empty");
        }

        // Создание комментария
        Comment comment = itemMapper.toComment(commentDto, itemId, userId);
        comment = commentRepository.save(comment);
        log.info("Comment created with id: {}", comment.getId());

        return itemMapper.toCommentDto(comment, user.getName());
    }

    // === Вспомогательные методы ===

    private void checkUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found with id: " + userId);
        }
    }

    private ItemWithBookingsDto buildItemWithBookingsAndComments(Item item, Long userId) {

        // Проверяем, является ли пользователь владельцем вещи
        boolean isOwner = item.getOwner().equals(userId);

        BookingShortDto lastBooking = null;
        BookingShortDto nextBooking = null;

        // Даты бронирований видит только владелец
        if (isOwner) {
            List<Booking> itemBookings = bookingRepository.findByItemIdOrderByStartAsc(item.getId());
            LocalDateTime now = LocalDateTime.now();

            // Последнее завершенное бронирование
            List<Booking> pastBookings = itemBookings.stream()
                    .filter(b -> b.getStatus() == BookingStatus.APPROVED && b.getEnd().isBefore(now))
                    .sorted(Comparator.comparing(Booking::getEnd).reversed())
                    .toList();

            if (!pastBookings.isEmpty()) {
                Booking last = pastBookings.get(0);
                lastBooking = new BookingShortDto(last.getId(), last.getBookerId());
            }

            // Ближайшее будущее бронирование
            List<Booking> futureBookings = itemBookings.stream()
                    .filter(b -> b.getStatus() == BookingStatus.APPROVED && b.getStart().isAfter(now))
                    .sorted(Comparator.comparing(Booking::getStart))
                    .toList();

            if (!futureBookings.isEmpty()) {
                Booking next = futureBookings.get(0);
                nextBooking = new BookingShortDto(next.getId(), next.getBookerId());
            }
        }

        // Комментарии видят все пользователи (без изменений)
        List<CommentDto> comments = commentRepository.findByItemIdOrderByCreatedDesc(item.getId())
                .stream()
                .map(comment -> {
                    User author = userRepository.findById(comment.getAuthorId()).orElse(null);
                    String authorName = author != null ? author.getName() : "Unknown";
                    return itemMapper.toCommentDto(comment, authorName);
                })
                .collect(Collectors.toList());

        return itemMapper.toItemWithBookingsDto(item, lastBooking, nextBooking, comments);
    }
}