package br.com.bbts.gpe.gpe_backend.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.dao.DataIntegrityViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestControllerAdvice
public class GlobalExceptionHandler {
        private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        @ExceptionHandler(InvalidFileException.class)
        public ResponseEntity<Map<String, Object>> handleInvalidFile(InvalidFileException exception) {
                Map<String, Object> response = new HashMap<>();
                response.put("status", 400);
                response.put("mensagem", exception.getMessage());
                return ResponseEntity.badRequest().body(response);
        }

        @ExceptionHandler(MaxUploadSizeExceededException.class)
        public ResponseEntity<Map<String, Object>> handleFileTooLarge(MaxUploadSizeExceededException exception) {
                Map<String, Object> response = new HashMap<>();
                response.put("status", 400);
                response.put("mensagem", "O arquivo excede o limite de 20 MB");
                return ResponseEntity.badRequest().body(response);
        }

        @ExceptionHandler(SignatureIntegrationException.class)
        public ResponseEntity<Map<String, Object>> handleSignatureIntegration(
                        SignatureIntegrationException exception) {
                Map<String, Object> response = new HashMap<>();
                response.put("status", 502);
                response.put("mensagem", exception.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(response);
        }

        @ExceptionHandler(DocumentStorageException.class)
        public ResponseEntity<Map<String, Object>> handleDocumentStorage(DocumentStorageException exception) {
                LOGGER.error("Falha no armazenamento de documentos", exception);
                Map<String, Object> response = new HashMap<>();
                response.put("status", 500);
                response.put("mensagem", "Não foi possível concluir a operação com o arquivo");
                return ResponseEntity.internalServerError().body(response);
        }

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