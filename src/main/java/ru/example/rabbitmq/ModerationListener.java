package ru.example.rabbitmq;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.example.config.RabbitConfig;
import ru.example.exception.CustomRuntimeException;
import ru.example.repo.ShortUrlRepo;
import ru.example.service.shortener.ShortenerService;

import java.time.LocalDate;

@Component
public class ModerationListener {
    private final ShortUrlRepo shortUrlRepo;
    private final ShortenerService shortenerService;
    private final RabbitSender rabbitSender;

    @Autowired
    public ModerationListener(ShortUrlRepo shortUrlRepo,
                              ShortenerService shortenerService,
                              RabbitSender rabbitSender) {
        this.shortUrlRepo = shortUrlRepo;
        this.shortenerService = shortenerService;
        this.rabbitSender = rabbitSender;
    }

    @RabbitListener(queues = RabbitConfig.MODERATION_QUEUE)
    public void handleModerationWithRetry(String shortCode) throws CustomRuntimeException {

        try {
            shortenerService.updateShortUrlWithRetry(shortCode,
                    shortUrl -> {
                        if (!shortUrl.getCreatedAt()
                                .isBefore(LocalDate.now().minusDays(3L))) {
                            shortUrl.setIsApproved(true);
                        }
                    });
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CustomRuntimeException("Interrupted during retry");
        } catch (Exception e) {
            rabbitSender.sendToModerationQueue(shortCode);
        }
    }
}
