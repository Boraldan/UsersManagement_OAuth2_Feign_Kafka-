package boraldan.account.config;

import boraldan.account.controller.feign.component.CustomErrorDecoder;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    /**
     * Создает и настраивает пользовательский декодер ошибок для Feign.
     *
     * @return экземпляр CustomErrorDecoder, который обрабатывает ошибки при вызове API.
     */
    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomErrorDecoder();
    }

    /**
     * Создает и настраивает стратегию повторных попыток для Feign.
     *
     * @return экземпляр Retryer.Default с заданными параметрами.
     *         Первоначальная задержка 100 мс, максимальная задержка 1000 мс и максимум 3 попытки.
     */
    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(100, 1000, 3);
    }
}
