package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    // Все бронирования пользователя (booker)
    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId);

    // Бронирования пользователя по статусу
    List<Booking> findByBookerIdAndStatusOrderByStartDesc(Long bookerId, BookingStatus status);

    // Текущие бронирования пользователя
    List<Booking> findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
            Long bookerId, LocalDateTime now1, LocalDateTime now2);

    // Будущие бронирования пользователя
    List<Booking> findByBookerIdAndStartAfterOrderByStartDesc(Long bookerId, LocalDateTime now);

    // Прошедшие бронирования пользователя
    List<Booking> findByBookerIdAndEndBeforeOrderByStartDesc(Long bookerId, LocalDateTime now);

    // Для владельца вещей — все бронирования его вещей
    @Query("SELECT b FROM Booking b WHERE b.itemId IN (SELECT i.id FROM Item i WHERE i.owner = :ownerId) ORDER BY b.start DESC")
    List<Booking> findAllByOwnerItems(@Param("ownerId") Long ownerId);

    // Для владельца по статусу
    @Query("SELECT b FROM Booking b WHERE b.itemId IN (SELECT i.id FROM Item i WHERE i.owner = :ownerId) AND b.status = :status ORDER BY b.start DESC")
    List<Booking> findAllByOwnerItemsAndStatus(@Param("ownerId") Long ownerId, @Param("status") BookingStatus status);

    // Текущие бронирования для владельца
    @Query("SELECT b FROM Booking b WHERE b.itemId IN (SELECT i.id FROM Item i WHERE i.owner = :ownerId) AND b.start < :now AND b.end > :now ORDER BY b.start DESC")
    List<Booking> findAllCurrentByOwnerItems(@Param("ownerId") Long ownerId, @Param("now") LocalDateTime now);

    // Будущие бронирования для владельца
    @Query("SELECT b FROM Booking b WHERE b.itemId IN (SELECT i.id FROM Item i WHERE i.owner = :ownerId) AND b.start > :now ORDER BY b.start DESC")
    List<Booking> findAllFutureByOwnerItems(@Param("ownerId") Long ownerId, @Param("now") LocalDateTime now);

    // Прошедшие бронирования для владельца
    @Query("SELECT b FROM Booking b WHERE b.itemId IN (SELECT i.id FROM Item i WHERE i.owner = :ownerId) AND b.end < :now ORDER BY b.start DESC")
    List<Booking> findAllPastByOwnerItems(@Param("ownerId") Long ownerId, @Param("now") LocalDateTime now);

    // Поиск бронирований по вещи
    List<Booking> findByItemIdOrderByStartAsc(Long itemId);

    // Проверка, бронировал ли пользователь вещь и завершилось ли бронирование
    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.bookerId = :userId AND b.itemId = :itemId AND b.status = 'APPROVED' AND b.end < :now")
    boolean existsApprovedBookingByUserAndItemAndEndBefore(
            @Param("userId") Long userId,
            @Param("itemId") Long itemId,
            @Param("now") LocalDateTime now);
}