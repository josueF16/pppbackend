package pe.edu.upeu.PPPBACKEND.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            BadCredentialsException.class,
            AccountStatusException.class
    })
    public ResponseEntity<Map<String, String>> credencialesInvalidas(
            Exception exception) {

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "mensaje",
                        "Correo o contraseña incorrectos, o cuenta no disponible"
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> datosInvalidos(
            MethodArgumentNotValidException exception) {

        List<String> errores = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error ->
                        error.getField() + ": " + error.getDefaultMessage()
                )
                .toList();

        return ResponseEntity.badRequest()
                .body(Map.of(
                        "mensaje", "Revisa los datos enviados",
                        "errores", errores
                ));
    }
}