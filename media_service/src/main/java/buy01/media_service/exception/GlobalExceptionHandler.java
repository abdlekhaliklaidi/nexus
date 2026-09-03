package buy01.media_service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> badRequest(
            IllegalArgumentException ex){

        return ResponseEntity.badRequest().body(

                new ErrorResponse(

                        LocalDateTime.now(),

                        HttpStatus.BAD_REQUEST.value(),

                        ex.getMessage()
                )

        );

    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorResponse> forbidden(
            SecurityException ex){

        return ResponseEntity.status(HttpStatus.FORBIDDEN)

                .body(

                        new ErrorResponse(

                                LocalDateTime.now(),

                                HttpStatus.FORBIDDEN.value(),

                                ex.getMessage()

                        )

                );

    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> notFound(
            RuntimeException ex){

        return ResponseEntity.status(HttpStatus.NOT_FOUND)

                .body(

                        new ErrorResponse(

                                LocalDateTime.now(),

                                HttpStatus.NOT_FOUND.value(),

                                ex.getMessage()

                        )

                );

    }

}