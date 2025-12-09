package com.starkkcoder.todo_api.test.integration.exceptions;

import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test/validation")
@Validated
public class DummyValidationController {

    @GetMapping
    public String validatePage(@RequestParam @Min(1) Integer page) {
        return "ok-" + page;
    }
}
