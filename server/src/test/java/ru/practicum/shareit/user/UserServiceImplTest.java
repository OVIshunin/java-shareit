package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = new User(1L, "John Doe", "john@example.com");
        userDto = new UserDto(1L, "John Doe", "john@example.com");
    }

    @Test
    void getAllUsers_shouldReturnListOfUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserDto> result = userService.getAllUsers();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getName()).isEqualTo("John Doe");
        assertThat(result.get(0).getEmail()).isEqualTo("john@example.com");

        verify(userRepository).findAll();
    }

    @Test
    void getAllUsers_shouldReturnEmptyList_whenNoUsers() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<UserDto> result = userService.getAllUsers();

        assertThat(result).isEmpty();
        verify(userRepository).findAll();
    }

    @Test
    void getUserById_shouldReturnUser_whenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDto result = userService.getUserById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isEqualTo("john@example.com");

        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_shouldThrowNotFoundException_whenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found with id: 99");

        verify(userRepository).findById(99L);
    }

    @Test
    void createUser_shouldSaveAndReturnUser_whenEmailIsUnique() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = userService.createUser(userDto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isEqualTo("john@example.com");

        verify(userRepository).existsByEmail("john@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_shouldThrowConflictException_whenEmailAlreadyExists() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(userDto))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email already exists: john@example.com");

        verify(userRepository).existsByEmail("john@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_shouldUpdateNameAndEmail_whenValidData() {
        UserDto updateDto = new UserDto(null, "Updated Name", "updated@example.com");
        User updatedUser = new User(1L, "Updated Name", "updated@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        UserDto result = userService.updateUser(1L, updateDto);

        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getEmail()).isEqualTo("updated@example.com");

        verify(userRepository).findById(1L);
        verify(userRepository).existsByEmail("updated@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_shouldUpdateOnlyName_whenEmailIsNull() {
        UserDto updateDto = new UserDto(null, "Updated Name", null);
        User updatedUser = new User(1L, "Updated Name", "john@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        UserDto result = userService.updateUser(1L, updateDto);

        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getEmail()).isEqualTo("john@example.com");

        verify(userRepository).findById(1L);
        verify(userRepository, never()).existsByEmail(any());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_shouldUpdateOnlyEmail_whenNameIsNull() {
        UserDto updateDto = new UserDto(null, null, "updated@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(new User(1L, "John Doe", "updated@example.com"));

        UserDto result = userService.updateUser(1L, updateDto);

        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isEqualTo("updated@example.com");

        verify(userRepository).findById(1L);
        verify(userRepository).existsByEmail("updated@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_shouldThrowConflictException_whenEmailAlreadyExists() {
        UserDto updateDto = new UserDto(null, "Updated Name", "existing@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(1L, updateDto))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Email already exists: existing@example.com");

        verify(userRepository).findById(1L);
        verify(userRepository).existsByEmail("existing@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_shouldThrowNotFoundException_whenUserDoesNotExist() {
        UserDto updateDto = new UserDto(null, "Updated Name", null);

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(99L, updateDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found with id: 99");

        verify(userRepository).findById(99L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_shouldDelete_whenUserExists() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_shouldThrowNotFoundException_whenUserDoesNotExist() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found with id: 99");

        verify(userRepository).existsById(99L);
        verify(userRepository, never()).deleteById(any());
    }
}