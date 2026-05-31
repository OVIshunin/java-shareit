package ru.practicum.shareit.booking.model;

public enum BookingStatus {
    WAITING,   // ожидает подтверждения
    APPROVED,  // подтверждено
    REJECTED,  // отклонено
    CANCELED   // отменено
}