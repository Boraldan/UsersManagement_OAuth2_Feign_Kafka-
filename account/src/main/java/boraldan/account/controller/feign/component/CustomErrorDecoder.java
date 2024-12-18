package boraldan.account.controller.feign.component;

import feign.Response;
import feign.RetryableException;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;

import java.util.Date;

public class CustomErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder defaultErrorDecoder = new Default();

    /**
     * Декодирует ошибку на основе ответа от сервиса.
     *
     * @param methodKey ключ метода, для которого произошла ошибка.
     * @param response  ответ от сервиса.
     * @return исключение, соответствующее ошибке.
     */
    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() == HttpStatus.SERVICE_UNAVAILABLE.value()) {
            return new RetryableException(
                    response.status(),
                    "503 Service Unavailable",
                    response.request().httpMethod(),
                    new Date(),
                    response.request());
        }
        return defaultErrorDecoder.decode(methodKey, response);
    }
}