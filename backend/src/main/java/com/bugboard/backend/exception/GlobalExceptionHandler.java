package com.bugboard.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Questo componente intercetta le eccezioni lanciate dal Service
 * e le trasforma in risposte HTTP standard, mantenendo la logica
 * del backend nascosta per esterni.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("error", ex.getMessage());
        
        // Se il messaggio indica che qualcosa non è stato trovato, si restituisce HTTP 404
        if (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("non trovat")) {
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
        
        // Altrimenti, per altri errori generici, si restituisce HTTP 500
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
