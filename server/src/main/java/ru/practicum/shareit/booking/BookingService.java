package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import java.util.List;

public interface BookingService {

    // Создание бронирования
    BookingDto createBooking(Long userId, BookingDto bookingDto);

    // Подтверждение/отклонение бронирования (только владелец вещи)
    BookingDto approveBooking(Long bookingId, Long userId, boolean approved);

    // Получение бронирования по ID (автор или владелец вещи)
    BookingDto getBookingById(Long bookingId, Long userId);

    // Получение всех бронирований пользователя с фильтрацией по статусу
    List<BookingDto> getUserBookings(Long userId, String state);

    // Получение всех бронирований вещей пользователя (владелец) с фильтрацией
    List<BookingDto> getOwnerBookings(Long userId, String state);
}