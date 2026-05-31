package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.dto.BookingShortDto;
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
import java.util.List;
import java.util.Objects;
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

        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        List<Item> items = itemRepository.findByOwnerOrderByIdAsc(owner);
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

        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new ValidationException("Item name cannot be empty");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            throw new ValidationException("Item description cannot be empty");
        }
        if (itemDto.getAvailable() == null) {
            throw new ValidationException("Item available status cannot be null");
        }

        Item item = itemMapper.toItem(itemDto, owner);
        item = itemRepository.save(item);
        log.info("Item created with id: {}", item.getId());

        return itemMapper.toItemDto(item);
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long itemId, Long userId, ItemDto itemDto) {
        log.info("Updating item id: {} by user: {}", itemId, userId);

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found with id: " + itemId));

        if (!Objects.equals(existingItem.getOwner().getId(), userId)) {
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

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found with id: " + itemId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        boolean hasBooked = bookingRepository.existsApprovedBookingByUserAndItemAndEndBefore(
                userId, itemId, LocalDateTime.now());

        if (!hasBooked) {
            throw new BadRequestException("User has not booked this item or booking is not completed");
        }

        if (commentDto.getText() == null || commentDto.getText().isBlank()) {
            throw new ValidationException("Comment text cannot be empty");
        }

        Comment comment = itemMapper.toComment(commentDto, itemId, userId);
        comment = commentRepository.save(comment);
        log.info("Comment created with id: {}", comment.getId());

        return itemMapper.toCommentDto(comment, user.getName());
    }

    // === Вспомогательные методы ===

    private ItemWithBookingsDto buildItemWithBookingsAndComments(Item item, Long userId) {

        boolean isOwner = Objects.equals(item.getOwner().getId(), userId);

        BookingShortDto lastBooking = null;
        BookingShortDto nextBooking = null;
        LocalDateTime now = LocalDateTime.now();

        if (isOwner) {
            // Исправлено: b.getBooker().getId() вместо b.getBookerId()
            lastBooking = bookingRepository.findLastApprovedBookingByItemId(item.getId(), now)
                    .map(b -> new BookingShortDto(b.getId(), b.getBooker().getId()))
                    .orElse(null);

            nextBooking = bookingRepository.findNextApprovedBookingByItemId(item.getId(), now)
                    .map(b -> new BookingShortDto(b.getId(), b.getBooker().getId()))
                    .orElse(null);
        }

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