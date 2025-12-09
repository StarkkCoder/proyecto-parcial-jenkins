package com.starkkcoder.todo_api.integration.exceptions;

import com.starkkcoder.todo_api.infrastructure.ports.ApiExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = com.starkkcoder.todo_api.test.integration.exceptions.DummyValidationController.class)
@Import(ApiExceptionHandler.class)
class ApiExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void whenValidRequestParam_thenReturnsOk() throws Exception {
        mockMvc.perform(get("/test/validation")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("ok-1"));
    }

    @Test
    void whenInvalidRequestParam_thenReturnsProblemDetails400() throws Exception {
        mockMvc.perform(get("/test/validation")
                        .param("page", "0")
                        .accept(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Parámetros de solicitud no válidos"))
                .andExpect(jsonPath("$.type").value("https://example.com/problems/constraint-violation"))
                .andExpect(jsonPath("$.errors", not(empty())))
                .andExpect(jsonPath("$.errors[0].property", containsString("page")))
                .andExpect(jsonPath("$.errors[0].message", not(emptyString())));
    }
}
