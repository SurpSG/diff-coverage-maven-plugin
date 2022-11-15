package com.example.b;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BApplication {

    public static void main(String[] args) {
        SpringApplication.run(BApplication.class, args);
    }




    public int A() {
        return 1;
    }

    public int test() {
        return 2;
    }

}
