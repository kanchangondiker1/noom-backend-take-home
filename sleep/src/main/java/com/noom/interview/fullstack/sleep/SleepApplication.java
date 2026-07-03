package com.noom.interview.fullstack.sleep;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SleepApplication {

    public static final String UNIT_TEST_PROFILE = "unittest";

    public static void main(String[] args) {
        SpringApplication.run(SleepApplication.class, args);
    }
}
