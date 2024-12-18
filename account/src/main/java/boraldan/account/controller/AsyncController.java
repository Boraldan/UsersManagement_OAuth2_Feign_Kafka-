package boraldan.account.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

@RestController
public class AsyncController {

    @GetMapping("/async-result")
    public DeferredResult<String> getAsyncResult() {
        DeferredResult<String> output = new DeferredResult<>();

        // Имитируем выполнение длительной задачи в отдельном потоке
        new Thread(() -> {
            try {
                Thread.sleep(2000); // симуляция задержки
                output.setResult("Долгая задача завершена!");
            } catch (InterruptedException e) {
                output.setErrorResult("Ошибка выполнения");
            }
        }).start();



        return output;
    }
}