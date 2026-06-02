package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.client.booking.BookingClient;
import ru.practicum.shareit.client.booking.BookingController;
import ru.practicum.shareit.client.booking.dto.BookItemRequestDto;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingClient bookingClient;

    // ========== Happy Path ==========

    @Test
    void getBookings_shouldReturnOk() throws Exception {
        when(bookingClient.getBookings(eq(1L), any(BookingState.class), eq(0), eq(10)))
                .thenReturn(org.springframework.http.ResponseEntity.ok("[]"));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "ALL"))
                .andExpect(status().isOk());
    }

    @Test
    void bookItem_shouldReturnOk() throws Exception {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(3);
        BookItemRequestDto dto = new BookItemRequestDto(1L, start, end);

        when(bookingClient.bookItem(eq(1L), any(BookItemRequestDto.class)))
                .thenReturn(org.springframework.http.ResponseEntity.status(HttpStatus.CREATED).body("{}"));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    void getBooking_shouldReturnOk() throws Exception {
        when(bookingClient.getBooking(1L, 1L))
                .thenReturn(org.springframework.http.ResponseEntity.ok("{}"));

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void approveBooking_shouldReturnOk() throws Exception {
        when(bookingClient.approveBooking(1L, 1L, true))
                .thenReturn(org.springframework.http.ResponseEntity.ok("{}"));

        mockMvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk());
    }

    // ========== Негативные сценарии ==========

    @Test
    void getBookings_shouldReturnBadRequest_whenInvalidState() throws Exception {
        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .param("state", "INVALID_STATE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBookings_shouldReturnBadRequest_whenUserIdHeaderMissing() throws Exception {
        mockMvc.perform(get("/bookings"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBooking_shouldReturnNotFound_whenBookingDoesNotExist() throws Exception {
        when(bookingClient.getBooking(1L, 999L))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Not Found"));

        mockMvc.perform(get("/bookings/999")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void approveBooking_shouldReturnNotFound_whenBookingDoesNotExist() throws Exception {
        when(bookingClient.approveBooking(1L, 999L, true))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Not Found"));

        mockMvc.perform(patch("/bookings/999")
                        .header("X-Sharer-User-Id", 1L)
                        .param("approved", "true"))
                .andExpect(status().isNotFound());
    }

    @Test
    void bookItem_shouldReturnBadRequest_whenStartIsNull() throws Exception {
        BookItemRequestDto dto = new BookItemRequestDto(1L, null, LocalDateTime.now().plusDays(3));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void bookItem_shouldReturnBadRequest_whenEndIsNull() throws Exception {
        BookItemRequestDto dto = new BookItemRequestDto(1L, LocalDateTime.now().plusDays(1), null);

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void bookItem_shouldReturnBadRequest_whenItemIdIsNull() throws Exception {
        BookItemRequestDto dto = new BookItemRequestDto(null, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(3));

        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }
}