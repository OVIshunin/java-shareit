package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ShareItGatewayTest {

    @Test
    void contextLoads() {
        // Проверяет, что контекст Spring загружается без ошибок
    }

    @Test
    void mainMethodStartsApplication() {
        // Просто вызываем main для покрытия
        ShareItGateway.main(new String[]{});
    }
}