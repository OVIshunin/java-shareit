package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.dto.UserDto;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    @Test
    void toUserDto_shouldConvertUserToUserDto() {
        User user = new User(1L, "John Doe", "john@example.com");

        UserDto result = UserMapper.toUserDto(user);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void toUserDto_shouldReturnNull_whenUserIsNull() {
        UserDto result = UserMapper.toUserDto(null);

        assertThat(result).isNull();
    }

    @Test
    void toUser_shouldConvertUserDtoToUser() {
        UserDto userDto = new UserDto(1L, "John Doe", "john@example.com");

        User result = UserMapper.toUser(userDto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void toUser_shouldReturnNull_whenUserDtoIsNull() {
        User result = UserMapper.toUser(null);

        assertThat(result).isNull();
    }
}