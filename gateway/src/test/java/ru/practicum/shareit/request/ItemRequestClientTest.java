package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@RestClientTest(ItemRequestClient.class)
class ItemRequestClientTest {

    @Autowired
    private ItemRequestClient requestClient;

    @Autowired
    private MockRestServiceServer mockServer;

    @Test
    void createRequest_shouldReturnResponse() {
        ItemRequestCreateDto dto = new ItemRequestCreateDto("Нужна дрель");

        mockServer.expect(requestTo(containsString("/requests")))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andExpect(content().json("{\"description\":\"Нужна дрель\"}"))
                .andRespond(withStatus(HttpStatus.CREATED));

        var response = requestClient.createRequest(1L, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        mockServer.verify();
    }

    @Test
    void getUserRequests_shouldReturnResponse() {
        mockServer.expect(requestTo(containsString("/requests")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        var response = requestClient.getUserRequests(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        mockServer.verify();
    }

    @Test
    void getAllRequests_shouldReturnResponse() {
        mockServer.expect(requestTo(containsString("/requests/all?from=0&size=10")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        var response = requestClient.getAllRequests(1L, 0, 10);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        mockServer.verify();
    }

    @Test
    void getRequestById_shouldReturnResponse() {
        mockServer.expect(requestTo(containsString("/requests/1")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andRespond(withSuccess("{\"id\":1,\"description\":\"Нужна дрель\"}", MediaType.APPLICATION_JSON));

        var response = requestClient.getRequestById(1L, 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        mockServer.verify();
    }
}