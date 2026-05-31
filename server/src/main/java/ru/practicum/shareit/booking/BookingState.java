package ru.practicum.shareit.booking;

public enum BookingState {
    ALL,        // все бронирования
    CURRENT,    // текущие (активные)
    PAST,       // завершенные
    FUTURE,     // будущие
    WAITING,    // ожидающие подтверждения
    REJECTED    // отклоненные
}