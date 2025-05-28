package ru.example.rabbitmq;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import ru.example.config.RabbitConfig;

@Component
@RequiredArgsConstructor
public class RabbitSender {
    private final RabbitTemplate rabbitTemplate;

    public void sendToModerationQueue(Object message) {
        rabbitTemplate.convertAndSend(
                RabbitConfig.MODERATION_EXCHANGE,
                RabbitConfig.ROUTING_KEY,
                message
        );
    }
}
