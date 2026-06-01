package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {

    @Mock
    private ItemRequestRepository requestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemRequestMapper requestMapper;

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemRequestServiceImpl requestService;

    private User requestor;
    private ItemRequest request;
    private ItemRequestDto requestDto;
    private ItemRequestDto createdRequestDto;
    private ItemDto itemDto;

    @BeforeEach
    void setUp() {
        requestor = new User(1L, "Requestor", "requestor@example.com");
        LocalDateTime now = LocalDateTime.now();

        request = new ItemRequest(1L, "Нужна дрель", requestor, now);

        requestDto = new ItemRequestDto();
        requestDto.setDescription("Нужна дрель");

        createdRequestDto = new ItemRequestDto();
        createdRequestDto.setId(1L);
        createdRequestDto.setDescription("Нужна дрель");
        createdRequestDto.setCreated(now);
        createdRequestDto.setRequestorId(1L);

        itemDto = new ItemDto();
        itemDto.setId(1L);
        itemDto.setName("Дрель");
        itemDto.setDescription("Мощная дрель");
        itemDto.setAvailable(true);
    }

    // ========== createRequest ==========

    @Test
    void createRequest_shouldCreateAndReturnRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(requestor));
        when(requestMapper.toEntity(requestDto, requestor)).thenReturn(request);
        when(requestRepository.save(any(ItemRequest.class))).thenReturn(request);
        when(requestMapper.toDto(request)).thenReturn(createdRequestDto);

        ItemRequestDto result = requestService.createRequest(1L, requestDto);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDescription()).isEqualTo("Нужна дрель");

        verify(userRepository).findById(1L);
        verify(requestRepository).save(any(ItemRequest.class));
        verify(requestMapper).toDto(request);
    }

    @Test
    void createRequest_shouldThrowNotFoundException_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> requestService.createRequest(99L, requestDto))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found with id: 99");
    }

    @Test
    void createRequest_shouldThrowValidationException_whenDescriptionIsBlank() {
        requestDto.setDescription("");
        when(userRepository.findById(1L)).thenReturn(Optional.of(requestor));

        assertThatThrownBy(() -> requestService.createRequest(1L, requestDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Request description cannot be empty");
    }

    @Test
    void createRequest_shouldThrowValidationException_whenDescriptionIsNull() {
        requestDto.setDescription(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(requestor));

        assertThatThrownBy(() -> requestService.createRequest(1L, requestDto))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Request description cannot be empty");
    }

    // ========== getUserRequests ==========

    @Test
    void getUserRequests_shouldReturnListOfRequests() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(requestor));
        when(requestRepository.findByRequestorIdOrderByCreatedDesc(1L)).thenReturn(List.of(request));
        when(itemRepository.findByRequestIn(anyList())).thenReturn(List.of());
        when(requestMapper.toDtoWithItems(eq(request), anyList())).thenReturn(createdRequestDto);

        List<ItemRequestDto> result = requestService.getUserRequests(1L);

        assertThat(result).hasSize(1);
        verify(requestRepository).findByRequestorIdOrderByCreatedDesc(1L);
    }

    @Test
    void getUserRequests_shouldThrowNotFoundException_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> requestService.getUserRequests(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found with id: 99");
    }

    @Test
    void getUserRequests_shouldReturnEmptyList_whenNoRequests() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(requestor));
        when(requestRepository.findByRequestorIdOrderByCreatedDesc(1L)).thenReturn(List.of());

        List<ItemRequestDto> result = requestService.getUserRequests(1L);

        assertThat(result).isEmpty();
    }

    // ========== getAllRequests ==========

    @Test
    void getAllRequests_shouldReturnListOfRequests() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(requestor));
        when(requestRepository.findByRequestorIdNotOrderByCreatedDesc(eq(1L), any(PageRequest.class)))
                .thenReturn(List.of(request));
        when(itemRepository.findByRequestIn(anyList())).thenReturn(List.of());
        when(requestMapper.toDtoWithItems(eq(request), anyList())).thenReturn(createdRequestDto);

        List<ItemRequestDto> result = requestService.getAllRequests(1L, 0, 10);

        assertThat(result).hasSize(1);
        verify(requestRepository).findByRequestorIdNotOrderByCreatedDesc(eq(1L), any(PageRequest.class));
    }

    @Test
    void getAllRequests_shouldThrowNotFoundException_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> requestService.getAllRequests(99L, 0, 10))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found with id: 99");
    }

    // ========== getRequestById ==========

    @Test
    void getRequestById_shouldReturnRequestWithItems() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(requestor));
        when(requestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(itemRepository.findByRequest(1L)).thenReturn(List.of());
        when(requestMapper.toDtoWithItems(eq(request), anyList())).thenReturn(createdRequestDto);

        ItemRequestDto result = requestService.getRequestById(1L, 1L);

        assertThat(result.getId()).isEqualTo(1L);
        verify(requestRepository).findById(1L);
        verify(itemRepository).findByRequest(1L);
    }

    @Test
    void getRequestById_shouldThrowNotFoundException_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> requestService.getRequestById(99L, 1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("User not found with id: 99");
    }

    @Test
    void getRequestById_shouldThrowNotFoundException_whenRequestNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(requestor));
        when(requestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> requestService.getRequestById(1L, 99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Request not found with id: 99");
    }
}