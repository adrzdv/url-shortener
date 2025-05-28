package ru.example.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String MODERATION_QUEUE = "moderation.queue";
    public static final String MODERATION_EXCHANGE = "moderation.exchange";
    public static final String ROUTING_KEY = "moderation.key";

    @Bean
    public Queue queue() {
        return new Queue(MODERATION_QUEUE);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(MODERATION_EXCHANGE);
    }

    @Bean
    public Binding binding(Queue queue, TopicExchange topicExchange) {
        return BindingBuilder.bind(queue).to(topicExchange).with(ROUTING_KEY);
    }

}
