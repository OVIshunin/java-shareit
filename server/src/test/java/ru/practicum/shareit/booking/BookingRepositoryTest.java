package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User owner;
    private User booker;
    private Item item;
    private Booking booking;

    @BeforeEach
    void setUp() {
        owner = new User(null, "Owner", "owner@example.com");
        booker = new User(null, "Booker", "booker@example.com");
        entityManager.persistAndFlush(owner);
        entityManager.persistAndFlush(booker);

        item = new Item(null, "Дрель", "Мощная дрель", true, owner, null);
        entityManager.persistAndFlush(item);

        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(3);
        booking = new Booking(null, start, end, item, booker, BookingStatus.WAITING);
        entityManager.persistAndFlush(booking);
    }

    // ========== Методы для User (booker) ==========

    @Test
    void findByBookerIdOrderByStartDesc_shouldReturnBookingsOfBooker() {
        List<Booking> bookings = bookingRepository.findByBookerIdOrderByStartDesc(booker.getId());

        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getBooker().getId()).isEqualTo(booker.getId());
    }

    @Test
    void findByBookerIdAndStatusOrderByStartDesc_shouldReturnBookingsByStatus() {
        List<Booking> bookings = bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                booker.getId(), BookingStatus.WAITING);

        assertThat(bookings).hasSize(1);
    }

    @Test
    void findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc_shouldReturnCurrentBookings() {
        // Создаём текущее бронирование (start < now < end)
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        Booking currentBooking = new Booking(null, start, end, item, booker, BookingStatus.APPROVED);
        entityManager.persistAndFlush(currentBooking);

        List<Booking> bookings = bookingRepository.findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
                booker.getId(), LocalDateTime.now(), LocalDateTime.now());

        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getId()).isEqualTo(currentBooking.getId());
    }

    @Test
    void findByBookerIdAndStartAfterOrderByStartDesc_shouldReturnFutureBookings() {
        LocalDateTime start = LocalDateTime.now().plusDays(5);
        LocalDateTime end = LocalDateTime.now().plusDays(7);
        Booking futureBooking = new Booking(null, start, end, item, booker, BookingStatus.APPROVED);
        entityManager.persistAndFlush(futureBooking);

        // Должно вернуть 2 бронирования:
        // 1. из setUp() (WAITING, start +1 день)
        // 2. новое (APPROVED, start +5 дней)
        List<Booking> bookings = bookingRepository.findByBookerIdAndStartAfterOrderByStartDesc(
                booker.getId(), LocalDateTime.now());

        assertThat(bookings).hasSize(2);
    }

    @Test
    void findByBookerIdAndEndBeforeOrderByStartDesc_shouldReturnPastBookings() {
        LocalDateTime start = LocalDateTime.now().minusDays(5);
        LocalDateTime end = LocalDateTime.now().minusDays(3);
        Booking pastBooking = new Booking(null, start, end, item, booker, BookingStatus.APPROVED);
        entityManager.persistAndFlush(pastBooking);

        List<Booking> bookings = bookingRepository.findByBookerIdAndEndBeforeOrderByStartDesc(
                booker.getId(), LocalDateTime.now());

        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getId()).isEqualTo(pastBooking.getId());
    }

    // ========== Методы для Owner ==========

    @Test
    void findAllByOwnerItems_shouldReturnBookingsOfOwnerItems() {
        List<Booking> bookings = bookingRepository.findAllByOwnerItems(owner.getId());

        assertThat(bookings).hasSize(1);
    }

    @Test
    void findAllByOwnerItemsAndStatus_shouldReturnBookingsByStatus() {
        List<Booking> bookings = bookingRepository.findAllByOwnerItemsAndStatus(
                owner.getId(), BookingStatus.WAITING);

        assertThat(bookings).hasSize(1);
    }

    @Test
    void findAllCurrentByOwnerItems_shouldReturnCurrentBookings() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);
        Booking currentBooking = new Booking(null, start, end, item, booker, BookingStatus.APPROVED);
        entityManager.persistAndFlush(currentBooking);

        List<Booking> bookings = bookingRepository.findAllCurrentByOwnerItems(
                owner.getId(), LocalDateTime.now());

        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getId()).isEqualTo(currentBooking.getId());
    }

    @Test
    void findAllFutureByOwnerItems_shouldReturnFutureBookings() {
        LocalDateTime start = LocalDateTime.now().plusDays(5);
        LocalDateTime end = LocalDateTime.now().plusDays(7);
        Booking futureBooking = new Booking(null, start, end, item, booker, BookingStatus.APPROVED);
        entityManager.persistAndFlush(futureBooking);

        // Должно вернуть 2 бронирования:
        // 1. из setUp() (WAITING, start +1 день)
        // 2. новое (APPROVED, start +5 дней)
        List<Booking> bookings = bookingRepository.findAllFutureByOwnerItems(
                owner.getId(), LocalDateTime.now());

        assertThat(bookings).hasSize(2);
    }

    @Test
    void findAllPastByOwnerItems_shouldReturnPastBookings() {
        LocalDateTime start = LocalDateTime.now().minusDays(5);
        LocalDateTime end = LocalDateTime.now().minusDays(3);
        Booking pastBooking = new Booking(null, start, end, item, booker, BookingStatus.APPROVED);
        entityManager.persistAndFlush(pastBooking);

        List<Booking> bookings = bookingRepository.findAllPastByOwnerItems(
                owner.getId(), LocalDateTime.now());

        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getId()).isEqualTo(pastBooking.getId());
    }

    // ========== Поиск по вещи ==========

    @Test
    void findByItemIdOrderByStartAsc_shouldReturnBookingsOrderedByStart() {
        List<Booking> bookings = bookingRepository.findByItemIdOrderByStartAsc(item.getId());

        assertThat(bookings).hasSize(1);
    }

    // ========== Проверка наличия завершённого бронирования ==========

    @Test
    void existsApprovedBookingByUserAndItemAndEndBefore_shouldReturnTrue() {
        LocalDateTime start = LocalDateTime.now().minusDays(5);
        LocalDateTime end = LocalDateTime.now().minusDays(3);
        Booking pastApprovedBooking = new Booking(null, start, end, item, booker, BookingStatus.APPROVED);
        entityManager.persistAndFlush(pastApprovedBooking);

        boolean exists = bookingRepository.existsApprovedBookingByUserAndItemAndEndBefore(
                booker.getId(), item.getId(), LocalDateTime.now());

        assertThat(exists).isTrue();
    }

    @Test
    void existsApprovedBookingByUserAndItemAndEndBefore_shouldReturnFalse_whenBookingNotApproved() {
        boolean exists = bookingRepository.existsApprovedBookingByUserAndItemAndEndBefore(
                booker.getId(), item.getId(), LocalDateTime.now());

        assertThat(exists).isFalse();
    }

    @Test
    void existsApprovedBookingByUserAndItemAndEndBefore_shouldReturnFalse_whenBookingNotEnded() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(3);
        Booking futureBooking = new Booking(null, start, end, item, booker, BookingStatus.APPROVED);
        entityManager.persistAndFlush(futureBooking);

        boolean exists = bookingRepository.existsApprovedBookingByUserAndItemAndEndBefore(
                booker.getId(), item.getId(), LocalDateTime.now());

        assertThat(exists).isFalse();
    }

    // ========== Last/Next бронирования ==========

    @Test
    void findLastApprovedBookingByItemId_shouldReturnLastBooking() {
        // Создаём два завершённых бронирования
        LocalDateTime start1 = LocalDateTime.now().minusDays(10);
        LocalDateTime end1 = LocalDateTime.now().minusDays(8);
        Booking oldBooking = new Booking(null, start1, end1, item, booker, BookingStatus.APPROVED);

        LocalDateTime start2 = LocalDateTime.now().minusDays(5);
        LocalDateTime end2 = LocalDateTime.now().minusDays(3);
        Booking recentBooking = new Booking(null, start2, end2, item, booker, BookingStatus.APPROVED);

        entityManager.persistAndFlush(oldBooking);
        entityManager.persistAndFlush(recentBooking);

        Optional<Booking> found = bookingRepository.findLastApprovedBookingByItemId(
                item.getId(), LocalDateTime.now());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(recentBooking.getId());
    }

    @Test
    void findLastApprovedBookingByItemId_shouldReturnEmpty_whenNoApprovedBookings() {
        Optional<Booking> found = bookingRepository.findLastApprovedBookingByItemId(
                item.getId(), LocalDateTime.now());

        assertThat(found).isEmpty();
    }

    @Test
    void findNextApprovedBookingByItemId_shouldReturnNextBooking() {
        // Создаём два будущих бронирования
        LocalDateTime start1 = LocalDateTime.now().plusDays(1);
        LocalDateTime end1 = LocalDateTime.now().plusDays(3);
        Booking soonBooking = new Booking(null, start1, end1, item, booker, BookingStatus.APPROVED);

        LocalDateTime start2 = LocalDateTime.now().plusDays(10);
        LocalDateTime end2 = LocalDateTime.now().plusDays(12);
        Booking laterBooking = new Booking(null, start2, end2, item, booker, BookingStatus.APPROVED);

        entityManager.persistAndFlush(soonBooking);
        entityManager.persistAndFlush(laterBooking);

        Optional<Booking> found = bookingRepository.findNextApprovedBookingByItemId(
                item.getId(), LocalDateTime.now());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(soonBooking.getId());
    }

    @Test
    void findNextApprovedBookingByItemId_shouldReturnEmpty_whenNoApprovedBookings() {
        Optional<Booking> found = bookingRepository.findNextApprovedBookingByItemId(
                item.getId(), LocalDateTime.now());

        assertThat(found).isEmpty();
    }

    // ========== JOIN FETCH методы ==========

    @Test
    void findByBookerIdWithItemAndBooker_shouldReturnBookingsWithItemAndBookerFetched() {
        List<Booking> bookings = bookingRepository.findByBookerIdWithItemAndBooker(booker.getId());

        assertThat(bookings).hasSize(1);
        // Проверяем, что item и booker загружены (не прокси)
        assertThat(bookings.get(0).getItem().getName()).isEqualTo("Дрель");
        assertThat(bookings.get(0).getBooker().getName()).isEqualTo("Booker");
    }

    @Test
    void findAllByOwnerWithItemAndBooker_shouldReturnBookingsWithItemAndBookerFetched() {
        List<Booking> bookings = bookingRepository.findAllByOwnerWithItemAndBooker(owner.getId());

        assertThat(bookings).hasSize(1);
        assertThat(bookings.get(0).getItem().getName()).isEqualTo("Дрель");
        assertThat(bookings.get(0).getBooker().getName()).isEqualTo("Booker");
    }
}