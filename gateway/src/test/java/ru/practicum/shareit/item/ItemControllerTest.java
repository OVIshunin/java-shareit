package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
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

    @Test
    void getUserItems_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());

        verify(itemClient).getUserItems(1L);
    }

    @Test
    void getItemById_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/items/10")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk());

        verify(itemClient).getItemById(1L, 10L);
    }

    @Test
    void createItem_shouldReturnOk_whenValid() throws Exception {
        ItemCreateDto dto = new ItemCreateDto("Дрель", "Мощная дрель", true, null);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(itemClient).createItem(eq(1L), any(ItemCreateDto.class));
    }

    @Test
    void createItem_shouldReturnBadRequest_whenNameBlank() throws Exception {
        ItemCreateDto dto = new ItemCreateDto("", "Мощная дрель", true, null);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createItem_shouldReturnBadRequest_whenDescriptionBlank() throws Exception {
        ItemCreateDto dto = new ItemCreateDto("Дрель", "", true, null);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createItem_shouldReturnBadRequest_whenAvailableNull() throws Exception {
        ItemCreateDto dto = new ItemCreateDto("Дрель", "Мощная дрель", null, null);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateItem_shouldReturnOk() throws Exception {
        ItemUpdateDto dto = new ItemUpdateDto("Обновлённая дрель", null, null);

        mockMvc.perform(patch("/items/10")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(itemClient).updateItem(eq(1L), eq(10L), any(ItemUpdateDto.class));
    }

    @Test
    void searchItems_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/items/search")
                        .param("text", "дрель"))
                .andExpect(status().isOk());

        verify(itemClient).searchItems("дрель");
    }

    @Test
    void addComment_shouldReturnOk_whenValid() throws Exception {
        CommentCreateDto dto = new CommentCreateDto("Отличная вещь!");

        mockMvc.perform(post("/items/10/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        verify(itemClient).addComment(eq(1L), eq(10L), any(CommentCreateDto.class));
    }

    @Test
    void addComment_shouldReturnBadRequest_whenTextBlank() throws Exception {
        CommentCreateDto dto = new CommentCreateDto("");

        mockMvc.perform(post("/items/10/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }
}