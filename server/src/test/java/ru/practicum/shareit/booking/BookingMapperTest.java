package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class BookingMapperTest {

    private final BookingMapper bookingMapper = new BookingMapper();

    @Test
    void toDto_shouldConvertBookingToBookingDto() {
        User owner = new User(1L, "Owner", "owner@example.com");
        User booker = new User(2L, "Booker", "booker@example.com");
        Item item = new Item(1L, "Дрель", "Мощная дрель", true, owner, null);
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(3);
        Booking booking = new Booking(1L, start, end, item, booker, BookingStatus.WAITING);

        BookingDto result = bookingMapper.toDto(booking, item, booker);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStart()).isEqualTo(start);
        assertThat(result.getEnd()).isEqualTo(end);
        assertThat(result.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(result.getItem().getId()).isEqualTo(1L);
        assertThat(result.getItem().getName()).isEqualTo("Дрель");
        assertThat(result.getBooker().getId()).isEqualTo(2L);
        assertThat(result.getBooker().getName()).isEqualTo("Booker");
    }

    @Test
    void toDto_shouldReturnNull_whenBookingIsNull() {
        BookingDto result = bookingMapper.toDto(null, null, null);

        assertThat(result).isNull();
    }

    @Test
    void toEntity_shouldConvertBookingDtoToBooking() {
        User owner = new User(1L, "Owner", "owner@example.com");
        User booker = new User(2L, "Booker", "booker@example.com");
        Item item = new Item(1L, "Дрель", "Мощная дрель", true, owner, null);
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(3);
        BookingDto dto = new BookingDto();
        dto.setStart(start);
        dto.setEnd(end);

        Booking result = bookingMapper.toEntity(dto, item, booker);

        assertThat(result.getStart()).isEqualTo(start);
        assertThat(result.getEnd()).isEqualTo(end);
        assertThat(result.getItem()).isEqualTo(item);
        assertThat(result.getBooker()).isEqualTo(booker);
    }

    @Test
    void toEntity_shouldReturnNull_whenDtoIsNull() {
        Booking result = bookingMapper.toEntity(null, null, null);

        assertThat(result).isNull();
    }

    @Test
    void toDtoWithEntity_shouldConvertBookingUsingItsFields() {
        User owner = new User(1L, "Owner", "owner@example.com");
        User booker = new User(2L, "Booker", "booker@example.com");
        Item item = new Item(1L, "Дрель", "Мощная дрель", true, owner, null);
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(3);
        Booking booking = new Booking(1L, start, end, item, booker, BookingStatus.WAITING);

        BookingDto result = bookingMapper.toDtoWithEntity(booking);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStart()).isEqualTo(start);
        assertThat(result.getEnd()).isEqualTo(end);
        assertThat(result.getStatus()).isEqualTo(BookingStatus.WAITING);
    }

    @Test
    void toDtoWithEntity_shouldReturnNull_whenBookingIsNull() {
        BookingDto result = bookingMapper.toDtoWithEntity(null);

        assertThat(result).isNull();
    }
}