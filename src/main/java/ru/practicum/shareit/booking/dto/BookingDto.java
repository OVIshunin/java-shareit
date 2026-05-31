package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.practicum.shareit.booking.model.BookingStatus;
import java.time.LocalDateTime;

@Data
public class BookingDto {
    private Long id;

    @NotNull
    @FutureOrPresent
    private LocalDateTime start;

    @NotNull
    @Future
    private LocalDateTime end;

    private Long itemId;

    // Для ответов
    private ItemBookingInfo item;
    private UserBookingInfo booker;
    private BookingStatus status;

    @Data
    public static class ItemBookingInfo {
        private Long id;
        private String name;
    }

    @Data
    public static class UserBookingInfo {
        private Long id;
        private String name;
    }
}
