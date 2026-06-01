package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemCreateDto;
import ru.practicum.shareit.item.dto.ItemUpdateDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@RestClientTest(ItemClient.class)
class ItemClientTest {

    @Autowired
    private ItemClient itemClient;

    @Autowired
    private MockRestServiceServer mockServer;

    @Test
    void getUserItems_shouldReturnResponse() {
        mockServer.expect(requestTo(containsString("/items")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        var response = itemClient.getUserItems(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        mockServer.verify();
    }

    @Test
    void getItemById_shouldReturnResponse() {
        mockServer.expect(requestTo(containsString("/items/1")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andRespond(withSuccess("{\"id\":1,\"name\":\"Дрель\"}", MediaType.APPLICATION_JSON));

        var response = itemClient.getItemById(1L, 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        mockServer.verify();
    }

    @Test
    void createItem_shouldReturnResponse() {
        ItemCreateDto dto = new ItemCreateDto("Дрель", "Мощная дрель", true, null);

        mockServer.expect(requestTo(containsString("/items")))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andExpect(content().json("{\"name\":\"Дрель\",\"description\":\"Мощная дрель\",\"available\":true}"))
                .andRespond(withStatus(HttpStatus.CREATED));

        var response = itemClient.createItem(1L, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        mockServer.verify();
    }

    @Test
    void updateItem_shouldReturnResponse() {
        ItemUpdateDto dto = new ItemUpdateDto("Обновлённая дрель", null, null);

        mockServer.expect(requestTo(containsString("/items/1")))
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andExpect(content().json("{\"name\":\"Обновлённая дрель\"}"))
                .andRespond(withSuccess("{\"id\":1,\"name\":\"Обновлённая дрель\"}", MediaType.APPLICATION_JSON));

        var response = itemClient.updateItem(1L, 1L, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        mockServer.verify();
    }

    @Test
    void searchItems_shouldReturnResponse() {
        mockServer.expect(requestTo(containsString("/items/search?text=")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[{\"id\":1,\"name\":\"Дрель\"}]", MediaType.APPLICATION_JSON));

        var response = itemClient.searchItems("дрель");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        mockServer.verify();
    }

    @Test
    void addComment_shouldReturnResponse() {
        CommentCreateDto dto = new CommentCreateDto("Отличная вещь!");

        mockServer.expect(requestTo(containsString("/items/1/comment")))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andExpect(content().json("{\"text\":\"Отличная вещь!\"}"))
                .andRespond(withStatus(HttpStatus.CREATED));

        var response = itemClient.addComment(1L, 1L, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        mockServer.verify();
    }
}