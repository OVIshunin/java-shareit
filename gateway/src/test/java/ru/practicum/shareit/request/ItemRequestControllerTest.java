package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestClient requestClient;

    // ========== Happy Path ==========

    @Test
    void createRequest_shouldReturnOk() throws Exception {
        ItemRequestCreateDto dto = new ItemRequestCreateDto("Нужна дрель");
        when(requestClient.createRequest(eq(1L), any(ItemRequestCreateDto.class)))
                .thenReturn(org.springframework.http.ResponseEntity.status(HttpStatus.CREATED).body("{}"));

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    void getUserRequests_shouldReturnOk() throws Exception {
        when(requestClient.getUserRequests(1L)).thenReturn(org.springframework.http.ResponseEntity.ok("[]"));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getAllRequests_shouldReturnOk() throws Exception {
        when(requestClient.getAllRequests(eq(1L), eq(0), eq(20)))
                .thenReturn(org.springframework.http.ResponseEntity.ok("[]"));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "20"))
                .andExpect(status().isOk());
    }

    @Test
    void getRequestById_shouldReturnOk() throws Exception {
        when(requestClient.getRequestById(1L, 1L)).thenReturn(org.springframework.http.ResponseEntity.ok("{}"));

        mockMvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    // ========== Негативные сценарии ==========

    @Test
    void getUserRequests_shouldReturnBadRequest_whenUserIdHeaderMissing() throws Exception {
        mockMvc.perform(get("/requests"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRequest_shouldReturnBadRequest_whenDescriptionIsBlank() throws Exception {
        ItemRequestCreateDto dto = new ItemRequestCreateDto("");

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRequest_shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
        ItemRequestCreateDto dto = new ItemRequestCreateDto("Нужна дрель");
        when(requestClient.createRequest(eq(999L), any(ItemRequestCreateDto.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Not Found"));

        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getRequestById_shouldReturnNotFound_whenRequestDoesNotExist() throws Exception {
        when(requestClient.getRequestById(1L, 999L))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Not Found"));

        mockMvc.perform(get("/requests/999")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllRequests_shouldReturnBadRequest_whenFromIsNegative() throws Exception {
        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "-1")
                        .param("size", "20"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllRequests_shouldReturnBadRequest_whenSizeIsZero() throws Exception {
        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .param("from", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }
}