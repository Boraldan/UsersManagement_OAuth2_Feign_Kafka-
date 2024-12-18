package boraldan.gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import reactor.core.publisher.Mono;

@Configuration
public class LoggingFilterConfig {

    private static final Logger logger = LoggerFactory.getLogger(LoggingFilterConfig.class);

    @Bean
    @Order(0)
    public GlobalFilter logRequestGlobalFilter() {
        return (exchange, chain) -> {
            String requestUrl = exchange.getRequest().getURI().toString();
            logger.info("Request URL: " + requestUrl);

            // Продолжаем цепочку фильтров
            return chain.filter(exchange)
                    .then(Mono.fromRunnable(() -> {
                        // Здесь можно дополнительно логировать ответ
                    }));
        };
    }
}