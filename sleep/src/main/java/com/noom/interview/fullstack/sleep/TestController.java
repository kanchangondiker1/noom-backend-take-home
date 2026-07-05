/*
 * Copyright (C) 2023 Noom, Inc.
 */
package com.noom.interview.fullstack.sleep;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/test")
    public Map<String, String> test() {
        return Map.of("testMessage", "Hello world!");
    }
}
