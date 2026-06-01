package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.comment.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    @Test
    void getUserItems_shouldReturnListOfItems() throws Exception {
        ItemWithBookingsDto item = new ItemWithBookingsDto();
        item.setId(1L);
        item.setName("Дрель");
        item.setDescription("Мощная дрель");
        item.setAvailable(true);

        when(itemService.getUserItems(1L)).thenReturn(List.of(item));

        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Дрель"));
    }

    @Test
    void getItemById_shouldReturnItem() throws Exception {
        ItemWithBookingsDto item = new ItemWithBookingsDto();
        item.setId(1L);
        item.setName("Дрель");
        item.setDescription("Мощная дрель");
        item.setAvailable(true);

        when(itemService.getItemById(1L, 1L)).thenReturn(item);

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Дрель"));
    }

    @Test
    void createItem_shouldReturnCreatedItem() throws Exception {
        ItemDto inputDto = new ItemDto(null, "Дрель", "Мощная дрель", true, null);
        ItemDto outputDto = new ItemDto(1L, "Дрель", "Мощная дрель", true, null);

        when(itemService.createItem(eq(1L), any(ItemDto.class))).thenReturn(outputDto);

        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Дрель"));
    }

    @Test
    void updateItem_shouldReturnUpdatedItem() throws Exception {
        ItemDto updateDto = new ItemDto(null, "Обновлённая дрель", null, null, null);
        ItemDto outputDto = new ItemDto(1L, "Обновлённая дрель", "Мощная дрель", true, null);

        when(itemService.updateItem(eq(1L), eq(1L), any(ItemDto.class))).thenReturn(outputDto);

        mockMvc.perform(patch("/items/1")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Обновлённая дрель"));
    }

    @Test
    void searchItems_shouldReturnListOfItems() throws Exception {
        ItemDto item = new ItemDto(1L, "Дрель", "Мощная дрель", true, null);

        when(itemService.search("дрель")).thenReturn(List.of(item));

        mockMvc.perform(get("/items/search")
                        .param("text", "дрель")
                        .header("X-Sharer-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Дрель"));
    }

    @Test
    void addComment_shouldReturnCreatedComment() throws Exception {
        CommentDto inputDto = new CommentDto();
        inputDto.setText("Отличная вещь!");

        CommentDto outputDto = new CommentDto();
        outputDto.setId(1L);
        outputDto.setText("Отличная вещь!");
        outputDto.setAuthorName("Booker");

        when(itemService.addComment(eq(1L), eq(1L), any(CommentDto.class))).thenReturn(outputDto);

        mockMvc.perform(post("/items/1/comment")
                        .header("X-Sharer-User-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Отличная вещь!"));
    }
}