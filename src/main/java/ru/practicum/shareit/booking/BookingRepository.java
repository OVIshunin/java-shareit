package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // === Методы для User (booker) — используем b.booker.id ===

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId ORDER BY b.start DESC")
    List<Booking> findByBookerIdOrderByStartDesc(@Param("bookerId") Long bookerId);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId AND b.status = :status ORDER BY b.start DESC")
    List<Booking> findByBookerIdAndStatusOrderByStartDesc(@Param("bookerId") Long bookerId,
                                                          @Param("status") BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId AND b.start < :now AND b.end > :now ORDER BY b.start DESC")
    List<Booking> findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
            @Param("bookerId") Long bookerId,
            @Param("now") LocalDateTime now1,
            @Param("now") LocalDateTime now2);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId AND b.start > :now ORDER BY b.start DESC")
    List<Booking> findByBookerIdAndStartAfterOrderByStartDesc(@Param("bookerId") Long bookerId,
                                                              @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId AND b.end < :now ORDER BY b.start DESC")
    List<Booking> findByBookerIdAndEndBeforeOrderByStartDesc(@Param("bookerId") Long bookerId,
                                                             @Param("now") LocalDateTime now);

    // === Методы для Owner (владелец вещей) ===

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId ORDER BY b.start DESC")
    List<Booking> findAllByOwnerItems(@Param("ownerId") Long ownerId);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.status = :status ORDER BY b.start DESC")
    List<Booking> findAllByOwnerItemsAndStatus(@Param("ownerId") Long ownerId,
                                               @Param("status") BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.start < :now AND b.end > :now ORDER BY b.start DESC")
    List<Booking> findAllCurrentByOwnerItems(@Param("ownerId") Long ownerId,
                                             @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.start > :now ORDER BY b.start DESC")
    List<Booking> findAllFutureByOwnerItems(@Param("ownerId") Long ownerId,
                                            @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.end < :now ORDER BY b.start DESC")
    List<Booking> findAllPastByOwnerItems(@Param("ownerId") Long ownerId,
                                          @Param("now") LocalDateTime now);

    // === Поиск по вещи ===

    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId ORDER BY b.start ASC")
    List<Booking> findByItemIdOrderByStartAsc(@Param("itemId") Long itemId);

    // === Проверка наличия завершённого бронирования ===

    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.booker.id = :userId AND b.item.id = :itemId AND b.status = 'APPROVED' AND b.end < :now")
    boolean existsApprovedBookingByUserAndItemAndEndBefore(
            @Param("userId") Long userId,
            @Param("itemId") Long itemId,
            @Param("now") LocalDateTime now);

    // === Last/Next бронирования для вещи ===

    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId " +
            "AND b.status = 'APPROVED' AND b.end < :now " +
            "ORDER BY b.end DESC LIMIT 1")
    Optional<Booking> findLastApprovedBookingByItemId(@Param("itemId") Long itemId,
                                                      @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.item.id = :itemId " +
            "AND b.status = 'APPROVED' AND b.start > :now " +
            "ORDER BY b.start ASC LIMIT 1")
    Optional<Booking> findNextApprovedBookingByItemId(@Param("itemId") Long itemId,
                                                      @Param("now") LocalDateTime now);

    // === НОВЫЕ МЕТОДЫ С JOIN FETCH (оптимизированные) ===

    // Для getUserBookings()
    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.item i " +
            "JOIN FETCH b.booker u " +
            "WHERE b.booker.id = :bookerId " +
            "ORDER BY b.start DESC")
    List<Booking> findByBookerIdWithItemAndBooker(@Param("bookerId") Long bookerId);

    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.item i " +
            "JOIN FETCH b.booker u " +
            "WHERE b.booker.id = :bookerId AND b.status = :status " +
            "ORDER BY b.start DESC")
    List<Booking> findByBookerIdAndStatusWithItemAndBooker(@Param("bookerId") Long bookerId,
                                                           @Param("status") BookingStatus status);

    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.item i " +
            "JOIN FETCH b.booker u " +
            "WHERE b.booker.id = :bookerId AND b.start < :now AND b.end > :now " +
            "ORDER BY b.start DESC")
    List<Booking> findByBookerIdAndCurrentWithItemAndBooker(@Param("bookerId") Long bookerId,
                                                            @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.item i " +
            "JOIN FETCH b.booker u " +
            "WHERE b.booker.id = :bookerId AND b.start > :now " +
            "ORDER BY b.start DESC")
    List<Booking> findByBookerIdAndFutureWithItemAndBooker(@Param("bookerId") Long bookerId,
                                                           @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.item i " +
            "JOIN FETCH b.booker u " +
            "WHERE b.booker.id = :bookerId AND b.end < :now " +
            "ORDER BY b.start DESC")
    List<Booking> findByBookerIdAndPastWithItemAndBooker(@Param("bookerId") Long bookerId,
                                                         @Param("now") LocalDateTime now);

    // Для getOwnerBookings()
    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.item i " +
            "JOIN FETCH b.booker u " +
            "WHERE i.owner.id = :ownerId " +
            "ORDER BY b.start DESC")
    List<Booking> findAllByOwnerWithItemAndBooker(@Param("ownerId") Long ownerId);

    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.item i " +
            "JOIN FETCH b.booker u " +
            "WHERE i.owner.id = :ownerId AND b.status = :status " +
            "ORDER BY b.start DESC")
    List<Booking> findAllByOwnerAndStatusWithItemAndBooker(@Param("ownerId") Long ownerId,
                                                           @Param("status") BookingStatus status);

    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.item i " +
            "JOIN FETCH b.booker u " +
            "WHERE i.owner.id = :ownerId AND b.start < :now AND b.end > :now " +
            "ORDER BY b.start DESC")
    List<Booking> findAllCurrentByOwnerWithItemAndBooker(@Param("ownerId") Long ownerId,
                                                         @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.item i " +
            "JOIN FETCH b.booker u " +
            "WHERE i.owner.id = :ownerId AND b.start > :now " +
            "ORDER BY b.start DESC")
    List<Booking> findAllFutureByOwnerWithItemAndBooker(@Param("ownerId") Long ownerId,
                                                        @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.item i " +
            "JOIN FETCH b.booker u " +
            "WHERE i.owner.id = :ownerId AND b.end < :now " +
            "ORDER BY b.start DESC")
    List<Booking> findAllPastByOwnerWithItemAndBooker(@Param("ownerId") Long ownerId,
                                                      @Param("now") LocalDateTime now);
}