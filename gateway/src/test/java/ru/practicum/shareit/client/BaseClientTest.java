package ru.practicum.shareit.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpStatusCodeException;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BaseClientTest {

    private TestBaseClient baseClient;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        baseClient = new TestBaseClient(restTemplate);
    }

    @Test
    void get_shouldReturnResponse() {
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok("Success");
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = baseClient.get("/test", 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void get_withParameters_shouldReturnResponse() {
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok("Success");
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(Object.class), any(Map.class)))
                .thenReturn(expectedResponse);

        Map<String, Object> params = Map.of("key", "value");
        ResponseEntity<Object> response = baseClient.get("/test?key={key}", 1L, params);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void post_shouldReturnResponse() {
        ResponseEntity<Object> expectedResponse = ResponseEntity.status(HttpStatus.CREATED).body("Created");
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = baseClient.post("/test", 1L, "body");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void patch_shouldReturnResponse() {
        ResponseEntity<Object> expectedResponse = ResponseEntity.ok("Updated");
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = baseClient.patch("/test", 1L, "body");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void delete_shouldReturnResponse() {
        ResponseEntity<Object> expectedResponse = ResponseEntity.noContent().build();
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(Object.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<Object> response = baseClient.delete("/test", 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void shouldHandleHttpStatusCodeException() {
        HttpStatusCodeException exception = HttpClientErrorException.create(
                HttpStatus.NOT_FOUND,
                "Not Found",
                null,
                "{\"error\":\"Not found\"}".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8);

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(Object.class)))
                .thenThrow(exception);

        ResponseEntity<Object> response = baseClient.get("/test", 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
    }

    // Тестовый класс-наследник для доступа к protected методам
    static class TestBaseClient extends BaseClient {
        public TestBaseClient(RestTemplate rest) {
            super(rest);
        }

        public ResponseEntity<Object> get(String path, Long userId) {
            return super.get(path, userId);
        }

        public ResponseEntity<Object> get(String path, Long userId, Map<String, Object> params) {
            return super.get(path, userId, params);
        }

        public <T> ResponseEntity<Object> post(String path, Long userId, T body) {
            return super.post(path, userId, body);
        }

        public <T> ResponseEntity<Object> patch(String path, Long userId, T body) {
            return super.patch(path, userId, body);
        }

        public ResponseEntity<Object> delete(String path, Long userId) {
            return super.delete(path, userId);
        }
    }
}