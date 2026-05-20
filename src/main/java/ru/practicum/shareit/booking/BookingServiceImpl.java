package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.exception.AccessDeniedException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingMapper bookingMapper;

    @Override
    @Transactional
    public BookingDto createBooking(Long userId, BookingDto bookingDto) {
        log.info("Creating booking for user id: {}, item id: {}", userId, bookingDto.getItemId());

        // Проверка существования пользователя
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        // Проверка существования вещи
        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Item not found with id: " + bookingDto.getItemId()));

        // Проверка: владелец не может бронировать свою вещь
        if (item.getOwner().equals(userId)) {
            throw new NotFoundException("Owner cannot book their own item");
        }

        // Проверка: вещь доступна для бронирования
        if (!item.getAvailable()) {
            throw new ValidationException("Item is not available for booking");
        }

        // Проверка дат
        validateBookingDates(bookingDto.getStart(), bookingDto.getEnd());

        // Создание бронирования
        Booking booking = bookingMapper.toEntity(bookingDto, item, booker);
        booking.setStatus(BookingStatus.WAITING);

        booking = bookingRepository.save(booking);
        log.info("Booking created with id: {}", booking.getId());

        return bookingMapper.toDto(booking, item, booker);
    }

    @Override
    @Transactional
    public BookingDto approveBooking(Long bookingId, Long userId, boolean approved) {
        log.info("Approving booking id: {} by user: {}, approved: {}", bookingId, userId, approved);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found with id: " + bookingId));

        // Проверка: только владелец вещи может подтверждать/отклонять
        Item item = itemRepository.findById(booking.getItemId())
                .orElseThrow(() -> new NotFoundException("Item not found"));

        if (!item.getOwner().equals(userId)) {
            throw new AccessDeniedException("Only item owner can approve or reject booking");
        }

        // Проверка: нельзя изменить уже подтвержденное или отклоненное бронирование
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Booking already " + booking.getStatus().toString().toLowerCase());
        }

        // Обновление статуса
        BookingStatus newStatus = approved ? BookingStatus.APPROVED : BookingStatus.REJECTED;
        booking.setStatus(newStatus);

        booking = bookingRepository.save(booking);
        log.info("Booking status updated to: {}", newStatus);

        User booker = userRepository.findById(booking.getBookerId())
                .orElseThrow(() -> new NotFoundException("Booker not found"));

        return bookingMapper.toDto(booking, item, booker);
    }

    @Override
    public BookingDto getBookingById(Long bookingId, Long userId) {
        log.debug("Getting booking id: {} for user: {}", bookingId, userId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found with id: " + bookingId));

        Item item = itemRepository.findById(booking.getItemId())
                .orElseThrow(() -> new NotFoundException("Item not found"));

        // Проверка: доступ имеют только автор бронирования или владелец вещи
        if (!booking.getBookerId().equals(userId) && !item.getOwner().equals(userId)) {
            throw new NotFoundException("Only booker or item owner can view booking");
        }

        User booker = userRepository.findById(booking.getBookerId())
                .orElseThrow(() -> new NotFoundException("Booker not found"));

        return bookingMapper.toDto(booking, item, booker);
    }

    @Override
    public List<BookingDto> getUserBookings(Long userId, String stateParam) {
        log.info("Getting user bookings for user id: {}, state: {}", userId, stateParam);

        // Проверка существования пользователя
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        BookingState state = parseState(stateParam);
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;

        switch (state) {
            case ALL:
                bookings = bookingRepository.findByBookerIdOrderByStartDesc(userId);
                break;
            case CURRENT:
                bookings = bookingRepository.findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                        userId, now, now);
                break;
            case PAST:
                bookings = bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(userId, now);
                break;
            case FUTURE:
                bookings = bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(userId, now);
                break;
            case WAITING:
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                        userId, BookingStatus.WAITING);
                break;
            case REJECTED:
                bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                        userId, BookingStatus.REJECTED);
                break;
            default:
                throw new ValidationException("Unknown state: " + stateParam);
        }

        return bookings.stream()
                .map(booking -> {
                    Item item = itemRepository.findById(booking.getItemId())
                            .orElseThrow(() -> new NotFoundException("Item not found"));
                    User booker = userRepository.findById(booking.getBookerId())
                            .orElseThrow(() -> new NotFoundException("Booker not found"));
                    return bookingMapper.toDto(booking, item, booker);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getOwnerBookings(Long userId, String stateParam) {
        log.info("Getting owner bookings for user id: {}, state: {}", userId, stateParam);

        // Проверка существования пользователя
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        // Проверка: у пользователя должна быть хотя бы одна вещь
        List<Item> userItems = itemRepository.findByOwnerOrderByIdAsc(userId);
        if (userItems.isEmpty()) {
            return List.of();
        }

        BookingState state = parseState(stateParam);
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings;

        switch (state) {
            case ALL:
                bookings = bookingRepository.findAllByOwnerItems(userId);
                break;
            case CURRENT:
                bookings = bookingRepository.findAllCurrentByOwnerItems(userId, now);
                break;
            case PAST:
                bookings = bookingRepository.findAllPastByOwnerItems(userId, now);
                break;
            case FUTURE:
                bookings = bookingRepository.findAllFutureByOwnerItems(userId, now);
                break;
            case WAITING:
                bookings = bookingRepository.findAllByOwnerItemsAndStatus(userId, BookingStatus.WAITING);
                break;
            case REJECTED:
                bookings = bookingRepository.findAllByOwnerItemsAndStatus(userId, BookingStatus.REJECTED);
                break;
            default:
                throw new ValidationException("Unknown state: " + stateParam);
        }

        return bookings.stream()
                .map(booking -> {
                    Item item = itemRepository.findById(booking.getItemId())
                            .orElseThrow(() -> new NotFoundException("Item not found"));
                    User booker = userRepository.findById(booking.getBookerId())
                            .orElseThrow(() -> new NotFoundException("Booker not found"));
                    return bookingMapper.toDto(booking, item, booker);
                })
                .collect(Collectors.toList());
    }

    // Вспомогательные методы

    private void validateBookingDates(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new ValidationException("Start and end dates cannot be null");
        }
        if (!start.isBefore(end)) {
            throw new ValidationException("End date must be after start date");
        }
        if (start.isBefore(LocalDateTime.now())) {
            throw new ValidationException("Start date cannot be in the past");
        }
    }

    private BookingState parseState(String stateParam) {
        if (stateParam == null || stateParam.isBlank()) {
            return BookingState.ALL;
        }
        try {
            return BookingState.valueOf(stateParam.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Unknown state: " + stateParam);
        }
    }
}
