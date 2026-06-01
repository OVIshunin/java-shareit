package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ShareItTests {

    @Test
    void contextLoads() {
    }

    @Test
    void mainMethodStartsApplication() {
        // Просто вызываем main, чтобы покрыть его
        ShareItServer.main(new String[]{});
    }

}