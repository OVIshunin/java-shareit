package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findByEmail_shouldReturnUser_whenEmailExists() {
        User user = new User(null, "John Doe", "john@example.com");
        entityManager.persistAndFlush(user);

        Optional<User> found = userRepository.findByEmail("john@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("John Doe");
    }

    @Test
    void findByEmail_shouldReturnEmpty_whenEmailDoesNotExist() {
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        assertThat(found).isEmpty();
    }

    @Test
    void existsByEmail_shouldReturnTrue_whenEmailExists() {
        User user = new User(null, "John Doe", "john@example.com");
        entityManager.persistAndFlush(user);

        boolean exists = userRepository.existsByEmail("john@example.com");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_shouldReturnFalse_whenEmailDoesNotExist() {
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        assertThat(exists).isFalse();
    }

    @Test
    void save_shouldPersistUser() {
        User user = new User(null, "John Doe", "john@example.com");

        User saved = userRepository.save(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("John Doe");
        assertThat(saved.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void findById_shouldReturnUser_whenUserExists() {
        User user = new User(null, "John Doe", "john@example.com");
        entityManager.persistAndFlush(user);

        Optional<User> found = userRepository.findById(user.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("John Doe");
    }

    @Test
    void findAll_shouldReturnAllUsers() {
        User user1 = new User(null, "John Doe", "john@example.com");
        User user2 = new User(null, "Jane Doe", "jane@example.com");
        entityManager.persistAndFlush(user1);
        entityManager.persistAndFlush(user2);

        var users = userRepository.findAll();

        assertThat(users).hasSize(2);
    }

    @Test
    void deleteById_shouldRemoveUser() {
        User user = new User(null, "John Doe", "john@example.com");
        entityManager.persistAndFlush(user);
        Long id = user.getId();

        userRepository.deleteById(id);
        entityManager.flush();

        Optional<User> found = userRepository.findById(id);
        assertThat(found).isEmpty();
    }
}