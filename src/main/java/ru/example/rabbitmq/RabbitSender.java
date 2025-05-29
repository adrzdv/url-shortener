package ru.example.rabbitmq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import ru.example.config.RabbitConfig;
import ru.example.dto.UrlModerationRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitSender {
    private final RabbitTemplate rabbitTemplate;

    public void sendToModerationQueue(UrlModerationRequest message) {
        rabbitTemplate.convertAndSend(
                RabbitConfig.MODERATION_EXCHANGE,
                RabbitConfig.ROUTING_KEY,
                message.getShortCode()
        );
    }
}
