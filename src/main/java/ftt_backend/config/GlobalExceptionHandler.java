package ftt_backend.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
        var fieldErr = ex.getBindingResult().getFieldError();
        var msg = fieldErr != null
                ? fieldErr.getDefaultMessage()
                : "입력값이 올바르지 않습니다.";
        return ResponseEntity.badRequest().body(msg);
    }
}
