package boraldan.account.controller.feign.component;

import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class FeignExceptionHandler {

    /**
     * Обработчик исключений FeignException.
     *
     * @param e исключение, возникшее в результате работы Feign-клиента.
     * @return ResponseEntity с соответствующим статусом и сообщением об ошибке.
     */
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<Object> handleFeignException(FeignException e) {
        HttpStatus status = switch (e.status()) {
            case 404 -> HttpStatus.NOT_FOUND;
            case 409 -> HttpStatus.CONFLICT;
            case 400 -> HttpStatus.BAD_REQUEST;
//            case 503 -> HttpStatus.SERVICE_UNAVAILABLE;   // опрабатываем  через CustomErrorDecoder
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };

        return ResponseEntity.status(status).body("Feign exception occurred: " + e.getMessage());
    }
}