package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User owner;

    @BeforeEach
    void setUp() {
        owner = new User(null, "Owner", "owner@example.com");
        entityManager.persistAndFlush(owner);
    }

    @Test
    void findByOwnerOrderByIdAsc_shouldReturnItemsOfOwner() {
        Item item1 = new Item(null, "Дрель", "Мощная дрель", true, owner, null);
        Item item2 = new Item(null, "Молоток", "Тяжёлый молоток", true, owner, null);
        entityManager.persistAndFlush(item1);
        entityManager.persistAndFlush(item2);

        List<Item> items = itemRepository.findByOwnerOrderByIdAsc(owner);

        assertThat(items).hasSize(2);
        assertThat(items.get(0).getName()).isEqualTo("Дрель");
        assertThat(items.get(1).getName()).isEqualTo("Молоток");
    }

    @Test
    void findByOwnerOrderByIdAsc_shouldReturnEmptyList_whenOwnerHasNoItems() {
        List<Item> items = itemRepository.findByOwnerOrderByIdAsc(owner);

        assertThat(items).isEmpty();
    }

    @Test
    void search_shouldReturnItemsMatchingName() {
        Item item1 = new Item(null, "Дрель", "Мощная дрель", true, owner, null);
        Item item2 = new Item(null, "Молоток", "Тяжёлый молоток", true, owner, null);
        entityManager.persistAndFlush(item1);
        entityManager.persistAndFlush(item2);

        List<Item> items = itemRepository.search("дрель");

        assertThat(items).hasSize(1);
        assertThat(items.get(0).getName()).isEqualTo("Дрель");
    }

    @Test
    void search_shouldReturnItemsMatchingDescription() {
        Item item1 = new Item(null, "Инструмент", "Мощная дрель для ремонта", true, owner, null);
        Item item2 = new Item(null, "Инструмент", "Тяжёлый молоток", true, owner, null);
        entityManager.persistAndFlush(item1);
        entityManager.persistAndFlush(item2);

        List<Item> items = itemRepository.search("дрель");

        assertThat(items).hasSize(1);
        assertThat(items.get(0).getDescription()).contains("дрель");
    }

    @Test
    void search_shouldReturnOnlyAvailableItems() {
        Item availableItem = new Item(null, "Дрель", "Мощная дрель", true, owner, null);
        Item unavailableItem = new Item(null, "Дрель", "Старая дрель", false, owner, null);
        entityManager.persistAndFlush(availableItem);
        entityManager.persistAndFlush(unavailableItem);

        List<Item> items = itemRepository.search("дрель");

        assertThat(items).hasSize(1);
        assertThat(items.get(0).getAvailable()).isTrue();
    }

    @Test
    void search_shouldReturnEmptyList_whenNoMatch() {
        Item item = new Item(null, "Дрель", "Мощная дрель", true, owner, null);
        entityManager.persistAndFlush(item);

        List<Item> items = itemRepository.search("молоток");

        assertThat(items).isEmpty();
    }

    @Test
    void search_shouldReturnEmptyList_whenTextIsEmpty() {
        Item item = new Item(null, "Дрель", "Мощная дрель", true, owner, null);
        entityManager.persistAndFlush(item);

        List<Item> items = itemRepository.search("");

        assertThat(items).hasSize(1);
    }

    @Test
    void findByRequest_shouldReturnItemsForRequest() {
        Long requestId = 10L;
        Item item1 = new Item(null, "Дрель", "Мощная дрель", true, owner, requestId);
        Item item2 = new Item(null, "Молоток", "Тяжёлый молоток", true, owner, null);
        entityManager.persistAndFlush(item1);
        entityManager.persistAndFlush(item2);

        List<Item> items = itemRepository.findByRequest(requestId);

        assertThat(items).hasSize(1);
        assertThat(items.get(0).getName()).isEqualTo("Дрель");
    }

    @Test
    void findByRequestIn_shouldReturnItemsForMultipleRequests() {
        Long requestId1 = 10L;
        Long requestId2 = 20L;
        Item item1 = new Item(null, "Дрель", "Мощная дрель", true, owner, requestId1);
        Item item2 = new Item(null, "Молоток", "Тяжёлый молоток", true, owner, requestId2);
        entityManager.persistAndFlush(item1);
        entityManager.persistAndFlush(item2);

        List<Item> items = itemRepository.findByRequestIn(List.of(requestId1, requestId2));

        assertThat(items).hasSize(2);
    }
}