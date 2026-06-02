package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpClientErrorException;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemClient itemClient;

    // ========== Happy Path ==========

    @Test
    void getUserItems_shouldReturnOk() throws Exception {
        when(itemClient.getUserItems(1L)).thenReturn(org.springframework.http.ResponseEntity.ok("[]"));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void getItemById_shouldReturnOk() throws Exception {
        when(itemClient.getItemById(1L, 1L)).thenReturn(org.springframework.http.ResponseEntity.ok("{}"));

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());
    }

    @Test
    void createItem_shouldReturnOk() throws Exception {
        ItemCreateDto dto = new ItemCreateDto("Дрель", "Мощная дрель", true, null);
        when(itemClient.createItem(eq(1L), any(ItemCreateDto.class)))
                .thenReturn(org.springframework.http.ResponseEntity.status(HttpStatus.CREATED).body("{}"));

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    void updateItem_shouldReturnOk() throws Exception {
        ItemUpdateDto dto = new ItemUpdateDto("Обновлённая дрель", null, null);
        when(itemClient.updateItem(eq(1L), eq(1L), any(ItemUpdateDto.class)))
                .thenReturn(org.springframework.http.ResponseEntity.ok("{}"));

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void searchItems_shouldReturnOk() throws Exception {
        when(itemClient.searchItems("дрель")).thenReturn(org.springframework.http.ResponseEntity.ok("[]"));

        mockMvc.perform(get("/items/search")
                        .param("text", "дрель"))
                .andExpect(status().isOk());
    }

    @Test
    void addComment_shouldReturnOk() throws Exception {
        CommentCreateDto dto = new CommentCreateDto("Отличная вещь!");
        when(itemClient.addComment(eq(1L), eq(1L), any(CommentCreateDto.class)))
                .thenReturn(org.springframework.http.ResponseEntity.status(HttpStatus.CREATED).body("{}"));

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    // ========== Негативные сценарии ==========

    @Test
    void getUserItems_shouldReturnBadRequest_whenUserIdHeaderMissing() throws Exception {
        mockMvc.perform(get("/items"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getItemById_shouldReturnNotFound_whenItemDoesNotExist() throws Exception {
        when(itemClient.getItemById(1L, 999L))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Not Found"));

        mockMvc.perform(get("/items/999")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    void createItem_shouldReturnBadRequest_whenNameIsBlank() throws Exception {
        ItemCreateDto dto = new ItemCreateDto("", "Мощная дрель", true, null);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createItem_shouldReturnBadRequest_whenDescriptionIsBlank() throws Exception {
        ItemCreateDto dto = new ItemCreateDto("Дрель", "", true, null);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createItem_shouldReturnBadRequest_whenAvailableIsNull() throws Exception {
        ItemCreateDto dto = new ItemCreateDto("Дрель", "Мощная дрель", null, null);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createItem_shouldReturnNotFound_whenUserDoesNotExist() throws Exception {
        ItemCreateDto dto = new ItemCreateDto("Дрель", "Мощная дрель", true, null);
        when(itemClient.createItem(eq(999L), any(ItemCreateDto.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Not Found"));

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateItem_shouldReturnNotFound_whenItemDoesNotExist() throws Exception {
        ItemUpdateDto dto = new ItemUpdateDto("Обновлённая дрель", null, null);
        when(itemClient.updateItem(eq(1L), eq(999L), any(ItemUpdateDto.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Not Found"));

        mockMvc.perform(patch("/items/999")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void addComment_shouldReturnBadRequest_whenTextIsBlank() throws Exception {
        CommentCreateDto dto = new CommentCreateDto("");

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }
}