package ru.practicum.shareit.item.comment;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentDto {
    private Long id;

    @NotBlank(message = "Comment text cannot be blank")
    private String text;

    private String authorName;
    private LocalDateTime created;
}