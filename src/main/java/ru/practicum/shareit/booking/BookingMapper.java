package ru.practicum.shareit.booking;


import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

@Component
public class BookingMapper {

    public BookingDto toDto(Booking booking, Item item, User booker) {
        if (booking == null) {
            return null;
        }

        BookingDto dto = new BookingDto();
        dto.setId(booking.getId());
        dto.setStart(booking.getStart());
        dto.setEnd(booking.getEnd());
        dto.setStatus(booking.getStatus());

        // Информация о вещи
        BookingDto.ItemBookingInfo itemInfo = new BookingDto.ItemBookingInfo();
        itemInfo.setId(item.getId());
        itemInfo.setName(item.getName());
        dto.setItem(itemInfo);

        // Информация о пользователе, который бронирует
        BookingDto.UserBookingInfo bookerInfo = new BookingDto.UserBookingInfo();
        bookerInfo.setId(booker.getId());
        bookerInfo.setName(booker.getName());
        dto.setBooker(bookerInfo);

        return dto;
    }

    public Booking toEntity(BookingDto dto, Item item, User booker) {
        if (dto == null) {
            return null;
        }

        Booking booking = new Booking();
        booking.setStart(dto.getStart());
        booking.setEnd(dto.getEnd());
        booking.setItemId(item.getId());
        booking.setBookerId(booker.getId());
        // Статус устанавливается в сервисе

        return booking;
    }
}