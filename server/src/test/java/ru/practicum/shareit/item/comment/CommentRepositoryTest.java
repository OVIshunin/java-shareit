package ru.practicum.shareit.item.comment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User author;
    private Item item;
    private Comment comment1;
    private Comment comment2;

    @BeforeEach
    void setUp() {
        User owner = new User(null, "Owner", "owner@example.com");
        author = new User(null, "Author", "author@example.com");
        entityManager.persistAndFlush(owner);
        entityManager.persistAndFlush(author);

        item = new Item(null, "Дрель", "Мощная дрель", true, owner, null);
        entityManager.persistAndFlush(item);

        comment1 = new Comment(null, "Отличная вещь!", item.getId(), author.getId(), LocalDateTime.now().minusDays(1));
        comment2 = new Comment(null, "Очень помогла!", item.getId(), author.getId(), LocalDateTime.now());

        entityManager.persistAndFlush(comment1);
        entityManager.persistAndFlush(comment2);
    }

    @Test
    void findByItemIdOrderByCreatedDesc_shouldReturnCommentsOrderedByCreatedDesc() {
        List<Comment> comments = commentRepository.findByItemIdOrderByCreatedDesc(item.getId());

        assertThat(comments).hasSize(2);
        assertThat(comments.get(0).getText()).isEqualTo("Очень помогла!");
        assertThat(comments.get(1).getText()).isEqualTo("Отличная вещь!");
    }

    @Test
    void findByItemIdOrderByCreatedDesc_shouldReturnEmptyList_whenNoComments() {
        List<Comment> comments = commentRepository.findByItemIdOrderByCreatedDesc(999L);

        assertThat(comments).isEmpty();
    }

    @Test
    void save_shouldPersistComment() {
        Comment newComment = new Comment(null, "Новый комментарий", item.getId(), author.getId(), LocalDateTime.now());

        Comment saved = commentRepository.save(newComment);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getText()).isEqualTo("Новый комментарий");
    }
}