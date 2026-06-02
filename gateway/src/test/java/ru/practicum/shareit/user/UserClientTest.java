package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@RestClientTest(UserClient.class)
class UserClientTest {

    @Autowired
    private UserClient userClient;

    @Autowired
    private MockRestServiceServer mockServer;

    @Test
    void getAllUsers_shouldReturnResponse() {
        mockServer.expect(requestTo(containsString("/users")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        var response = userClient.getAllUsers();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        mockServer.verify();
    }

    @Test
    void getUserById_shouldReturnResponse() {
        mockServer.expect(requestTo(containsString("/users/1")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"id\":1,\"name\":\"John\"}", MediaType.APPLICATION_JSON));

        var response = userClient.getUserById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        mockServer.verify();
    }

    @Test
    void createUser_shouldReturnResponse() {
        UserCreateDto dto = new UserCreateDto("John", "john@example.com");

        mockServer.expect(requestTo(containsString("/users")))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json("{\"name\":\"John\",\"email\":\"john@example.com\"}"))
                .andRespond(withStatus(HttpStatus.CREATED));

        var response = userClient.createUser(dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        mockServer.verify();
    }

    @Test
    void updateUser_shouldReturnResponse() {
        UserUpdateDto dto = new UserUpdateDto("Updated", null);

        mockServer.expect(requestTo(containsString("/users/1")))
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(content().json("{\"name\":\"Updated\"}"))
                .andRespond(withSuccess("{\"id\":1,\"name\":\"Updated\"}", MediaType.APPLICATION_JSON));

        var response = userClient.updateUser(1L, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        mockServer.verify();
    }

    @Test
    void deleteUser_shouldReturnResponse() {
        mockServer.expect(requestTo(containsString("/users/1")))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withStatus(HttpStatus.NO_CONTENT));

        var response = userClient.deleteUser(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        mockServer.verify();
    }
}