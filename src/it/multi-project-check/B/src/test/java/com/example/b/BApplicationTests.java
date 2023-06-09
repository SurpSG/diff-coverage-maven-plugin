package com.example.b;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import sun.jvm.hotspot.utilities.Assert;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
class BApplicationTests {

    @Autowired
    private BApplication b;

    @Test
    void contextLoads() {
    }

    @Test
    public void doesItWork() {
        assertEquals(1, b.A());
        assertEquals(2, b.test());
    }

}
