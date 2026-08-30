package com.aicitybrain;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * If this fails, something in the wiring is broken (missing bean, circular
 * dependency, bad property, etc.) — the cheapest possible test with the highest
 * signal, and the first thing worth running after any change to configuration.
 */
@SpringBootTest
@ActiveProfiles("test")
class AiCityBrainApplicationTests {

    @Test
    void contextLoads() {
    }
}
