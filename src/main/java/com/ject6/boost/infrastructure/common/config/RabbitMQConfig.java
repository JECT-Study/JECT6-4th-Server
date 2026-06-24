package com.ject6.boost.infrastructure.common.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String ANALYSIS_QUEUE = "blog.analysis";
    public static final String ANALYSIS_DLX   = "blog.analysis.dlx";
    public static final String ANALYSIS_DLQ   = "blog.analysis.dlq";

    public static final String COMPLETED_QUEUE = "blog.analysis.completed";
    public static final String COMPLETED_DLX   = "blog.analysis.completed.dlx";
    public static final String COMPLETED_DLQ   = "blog.analysis.completed.dlq";

    @Bean
    public Queue analysisQueue() {
        return QueueBuilder.durable(ANALYSIS_QUEUE)
                .withArgument("x-dead-letter-exchange", ANALYSIS_DLX)
                .build();
    }

    @Bean
    public FanoutExchange analysisDlx() {
        return new FanoutExchange(ANALYSIS_DLX, true, false);
    }

    @Bean
    public Queue analysisDlq() {
        return QueueBuilder.durable(ANALYSIS_DLQ).build();
    }

    @Bean
    public Binding analysisDlqBinding() {
        return BindingBuilder.bind(analysisDlq()).to(analysisDlx());
    }

    @Bean
    public Queue completedQueue() {
        return QueueBuilder.durable(COMPLETED_QUEUE)
                .withArgument("x-dead-letter-exchange", COMPLETED_DLX)
                .build();
    }

    @Bean
    public FanoutExchange completedDlx() {
        return new FanoutExchange(COMPLETED_DLX, true, false);
    }

    @Bean
    public Queue completedDlq() {
        return QueueBuilder.durable(COMPLETED_DLQ).build();
    }

    @Bean
    public Binding completedDlqBinding() {
        return BindingBuilder.bind(completedDlq()).to(completedDlx());
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
