package com.example.manicure_backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // 👈 Isso força o teste a ler o application-test.properties
class ManicureBackendApplicationTests {
    @Test
    void contextLoads() {
    }
}
