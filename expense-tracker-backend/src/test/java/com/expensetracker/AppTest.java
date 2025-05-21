package com.expensetracker;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for simple App.
 */
@SpringBootTest
class AppTest {
    @Test
    void contextLoads() {
        assertThat(true).isTrue();
    }
}
