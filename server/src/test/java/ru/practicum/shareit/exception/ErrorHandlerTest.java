package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.UserService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class ErrorHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void shouldReturnForbidden_whenAccessDeniedExceptionThrown() throws Exception {
        when(userService.updateUser(any(), any()))
                .thenThrow(new AccessDeniedException("User is not the owner of this item"));

        String json = "{\"name\":\"New Name\"}";

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch("/users/1")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("User is not the owner of this item"));
    }

    @Test
    void shouldReturnNotFound_whenNotFoundExceptionThrown() throws Exception {
        when(userService.getUserById(any())).thenThrow(new NotFoundException("User not found with id: 99"));

        mockMvc.perform(get("/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found with id: 99"));
    }

    @Test
    void shouldReturnBadRequest_whenIllegalArgumentExceptionThrown() throws Exception {
        when(userService.createUser(any())).thenThrow(new IllegalArgumentException("Invalid argument"));

        String json = "{\"name\":\"Test\",\"email\":\"test@example.com\"}";

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid argument"));
    }

    @Test
    void shouldReturnBadRequest_whenIllegalStateExceptionThrown() throws Exception {
        when(userService.createUser(any())).thenThrow(new IllegalStateException("Illegal state"));

        String json = "{\"name\":\"Test\",\"email\":\"test@example.com\"}";

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Illegal state"));
    }

    @Test
    void shouldReturnConflict_whenConflictExceptionThrown() throws Exception {
        when(userService.createUser(any()))
                .thenThrow(new ConflictException("Email already exists: test@example.com"));

        String json = "{\"name\":\"Test\",\"email\":\"test@example.com\"}";

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Email already exists: test@example.com"));
    }

    @Test
    void shouldReturnBadRequest_whenValidationExceptionThrown() throws Exception {
        when(userService.createUser(any())).thenThrow(new ValidationException("Item name cannot be empty"));

        String json = "{\"name\":\"\",\"email\":\"test@example.com\"}";

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Item name cannot be empty"));
    }

    @Test
    void shouldReturnBadRequest_whenBadRequestExceptionThrown() throws Exception {
        when(userService.createUser(any())).thenThrow(new BadRequestException("User has not booked this item"));

        String json = "{\"name\":\"Test\",\"email\":\"test@example.com\"}";

        mockMvc.perform(post("/users")
                        .contentType("application/json")
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("User has not booked this item"));
    }

    @Test
    void shouldReturnInternalServerError_whenGenericExceptionThrown() throws Exception {
        when(userService.getUserById(any())).thenThrow(new RuntimeException("Database connection failed"));

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("Internal server error: Database connection failed"));
    }
}