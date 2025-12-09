package com.starkkcoder.todo_api.infrastructure.ports;

import com.starkkcoder.todo_api.application.exceptions.TodoNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(TodoNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleTodoNotFound(TodoNotFoundException ex,
                                                            HttpServletRequest request) {

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );

        problem.setTitle("Todo no encontrada");
        problem.setType(URI.create("https://example.com/problems/todo-not-found"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("errorCode", "TODO_NOT_FOUND");

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                      HttpServletRequest request) {

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Datos de entrada no válidos");
        problem.setType(URI.create("https://example.com/problems/validation-error"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setDetail("Uno o más campos contienen errores de validación.");

        List<Map<String, Object>> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> Map.<String, Object>of(
                        "field", fieldError.getField(),
                        "rejectedValue", fieldError.getRejectedValue(),
                        "message", fieldError.getDefaultMessage()
                ))
                .collect(Collectors.toList());

        problem.setProperty("errors", errors); // extensión del problema

        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(ConstraintViolationException ex,
                                                                   HttpServletRequest request) {

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Parámetros de solicitud no válidos");
        problem.setType(URI.create("https://example.com/problems/constraint-violation"));
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setDetail("Se encontraron violaciones de restricciones en los parámetros.");

        List<Map<String, Object>> violations = ex.getConstraintViolations()
                .stream()
                .map(v -> Map.<String, Object>of(
                        "property", v.getPropertyPath().toString(),
                        "rejectedValue", v.getInvalidValue(),
                        "message", v.getMessage()
                ))
                .collect(Collectors.toList());

        problem.setProperty("errors", violations);

        return ResponseEntity.badRequest().body(problem);
    }
}
