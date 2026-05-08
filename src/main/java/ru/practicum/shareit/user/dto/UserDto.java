package ru.practicum.shareit.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.validation.OnCreate;
import ru.practicum.shareit.validation.OnUpdate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

    private Long id;

    @NotBlank(groups = OnCreate.class)  // только при создании
    private String name;

    @NotBlank(groups = OnCreate.class)
    @Email(groups = {OnCreate.class, OnUpdate.class})  // формат проверяем всегда
    private String email;
}