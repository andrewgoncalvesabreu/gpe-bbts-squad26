package br.com.bbts.gpe.gpe_backend.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.dao.DataIntegrityViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<Map<String, Object>> handleNotFound(
                        ResourceNotFoundException exception) {

                Map<String, Object> resposta = new HashMap<>();

                resposta.put("status", 404);
                resposta.put("mensagem", exception.getMessage());

                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(resposta);
        }

        @ExceptionHandler(BusinessException.class)
        public ResponseEntity<Map<String, Object>> handleBusinessException(
                        BusinessException exception) {

                Map<String, Object> resposta = new HashMap<>();

                resposta.put("status", 409);
                resposta.put("mensagem", exception.getMessage());

                return ResponseEntity
                                .status(HttpStatus.CONFLICT)
                                .body(resposta);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<Map<String, Object>> handleValidation(
                        MethodArgumentNotValidException exception) {

                Map<String, String> erros = new HashMap<>();

                exception.getBindingResult()
                                .getFieldErrors()
                                .forEach(error -> erros.put(
                                                error.getField(),
                                                error.getDefaultMessage()));

                Map<String, Object> resposta = new HashMap<>();

                resposta.put("status", 400);
                resposta.put("mensagem", "Dados inválidos");
                resposta.put("erros", erros);

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(resposta);
        }

        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<Map<String, Object>> handleIntegrity(
                        DataIntegrityViolationException exception) {

                Map<String, Object> resposta = new HashMap<>();
                resposta.put("status", 409);
                resposta.put("mensagem",
                                "Operação não permitida: registro duplicado ou vinculado a outros dados.");

                return ResponseEntity.status(HttpStatus.CONFLICT).body(resposta);
        }
}