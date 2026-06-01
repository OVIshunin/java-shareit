package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingServiceImpl bookingService;

    private User owner;
    private User booker;
    private Item item;
    private Booking booking;
    private BookingDto bookingDto;
    private BookingDto responseBookingDto;

    @BeforeEach
    void setUp() {
        owner = new User(1L, "Owner", "owner@example.com");
        booker = new User(2L, "Booker", "booker@example.com");

        item = new Item(1L, "Дрель", "Мощная дрель", true, owner, null);

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(3);

        booking = new Booking(1L, start, end, item, booker, BookingStatus.WAITING);

        bookingDto = new BookingDto();
        bookingDto.setItemId(1L);
        bookingDto.setStart(start);
        bookingDto.setEnd(end);

        responseBookingDto = new BookingDto();
        responseBookingDto.setId(1L);
        responseBookingDto.setStart(start);
        responseBookingDto.setEnd(end);
        responseBookingDto.setStatus(BookingStatus.WAITING);
    }

    // ========== createBooking ==========

    @Test
    void createBooking_shouldCreateAndReturnBooking() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(bookingMapper.toEntity(bookingDto, item, booker)).thenReturn(booking);
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.toDto(booking, item, booker)).thenReturn(responseBookingDto);

        BookingDto result = bookingService.createBooking(2L, bookingDto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(BookingStatus.WAITING);

        verify(userRepository).findById(2L);
        verify(itemRepository).findById(1L);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void createBooking_shouldThrowNotFoundException_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(99L, bookingDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found with id: 99");
    }

    @Test
    void createBooking_shouldThrowNotFoundException_whenItemNotFound() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        bookingDto.setItemId(99L);  // ← добавить эту строку
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.createBooking(2L, bookingDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Item not found with id: 99");
    }

    @Test
    void createBooking_shouldThrowNotFoundException_whenOwnerTriesToBookOwnItem() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.createBooking(1L, bookingDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Owner cannot book their own item");
    }

    @Test
    void createBooking_shouldThrowValidationException_whenItemIsNotAvailable() {
        item.setAvailable(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.createBooking(2L, bookingDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Item is not available for booking");
    }

    @Test
    void createBooking_shouldThrowValidationException_whenStartIsNull() {
        bookingDto.setStart(null);
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.createBooking(2L, bookingDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Start and end dates cannot be null");
    }

    @Test
    void createBooking_shouldThrowValidationException_whenEndIsNull() {
        bookingDto.setEnd(null);
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.createBooking(2L, bookingDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Start and end dates cannot be null");
    }

    @Test
    void createBooking_shouldThrowValidationException_whenEndBeforeStart() {
        bookingDto.setStart(LocalDateTime.now().plusDays(3));
        bookingDto.setEnd(LocalDateTime.now().plusDays(1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.createBooking(2L, bookingDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("End date must be after start date");
    }

    @Test
    void createBooking_shouldThrowValidationException_whenStartInPast() {
        bookingDto.setStart(LocalDateTime.now().minusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(3));
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> bookingService.createBooking(2L, bookingDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Start date cannot be in the past");
    }

    // ========== approveBooking ==========

    @Test
    void approveBooking_shouldApproveBooking_whenOwnerApproves() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.toDto(booking, item, booker)).thenReturn(responseBookingDto);

        BookingDto result = bookingService.approveBooking(1L, 1L, true);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.WAITING);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void approveBooking_shouldRejectBooking_whenOwnerRejects() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(bookingMapper.toDto(booking, item, booker)).thenReturn(responseBookingDto);

        BookingDto result = bookingService.approveBooking(1L, 1L, false);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.WAITING);
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void approveBooking_shouldThrowNotFoundException_whenBookingNotFound() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.approveBooking(99L, 1L, true))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Booking not found with id: 99");
    }

    @Test
    void approveBooking_shouldThrowAccessDeniedException_whenUserIsNotOwner() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.approveBooking(1L, 99L, true))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Only item owner can approve or reject booking");
    }

    @Test
    void approveBooking_shouldThrowValidationException_whenBookingAlreadyApproved() {
        booking.setStatus(BookingStatus.APPROVED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.approveBooking(1L, 1L, true))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Booking already approved");
    }

    @Test
    void approveBooking_shouldThrowValidationException_whenBookingAlreadyRejected() {
        booking.setStatus(BookingStatus.REJECTED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.approveBooking(1L, 1L, true))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Booking already rejected");
    }

    // ========== getBookingById ==========

    @Test
    void getBookingById_shouldReturnBooking_whenUserIsBooker() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingMapper.toDto(booking, item, booker)).thenReturn(responseBookingDto);

        BookingDto result = bookingService.getBookingById(1L, 2L);

        assertThat(result.getId()).isEqualTo(1L);
        verify(bookingMapper).toDto(booking, item, booker);
    }

    @Test
    void getBookingById_shouldReturnBooking_whenUserIsOwner() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingMapper.toDto(booking, item, booker)).thenReturn(responseBookingDto);

        BookingDto result = bookingService.getBookingById(1L, 1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getBookingById_shouldThrowNotFoundException_whenUserIsNotBookerOrOwner() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.getBookingById(1L, 99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Only booker or item owner can view booking");
    }

    @Test
    void getBookingById_shouldThrowNotFoundException_whenBookingNotFound() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getBookingById(99L, 1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Booking not found with id: 99");
    }

    // ========== getUserBookings ==========

    @Test
    void getUserBookings_shouldReturnListOfBookings() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findByBookerIdWithItemAndBooker(2L)).thenReturn(List.of(booking));
        when(bookingMapper.toDtoWithEntity(booking)).thenReturn(responseBookingDto);

        List<BookingDto> result = bookingService.getUserBookings(2L, "ALL");

        assertThat(result).hasSize(1);
        verify(bookingRepository).findByBookerIdWithItemAndBooker(2L);
    }

    @Test
    void getUserBookings_shouldThrowValidationException_whenUnknownState() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));

        assertThatThrownBy(() -> bookingService.getUserBookings(2L, "UNKNOWN"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Unknown state: UNKNOWN");
    }

    // ========== getOwnerBookings ==========

    @Test
    void getOwnerBookings_shouldReturnListOfOwnerBookings() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findAllByOwnerWithItemAndBooker(1L)).thenReturn(List.of(booking));
        when(bookingMapper.toDtoWithEntity(booking)).thenReturn(responseBookingDto);

        List<BookingDto> result = bookingService.getOwnerBookings(1L, "ALL");

        assertThat(result).hasSize(1);
        verify(bookingRepository).findAllByOwnerWithItemAndBooker(1L);
    }

    @Test
    void getOwnerBookings_shouldThrowNotFoundException_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getOwnerBookings(99L, "ALL"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found with id: 99");
    }
}