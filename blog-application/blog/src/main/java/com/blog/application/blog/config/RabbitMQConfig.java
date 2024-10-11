package com.blog.application.blog.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class RabbitMQConfig {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMQConfig.class);

    private final RabbitMQProperties properties;
    private final AmqpAdmin amqpAdmin;

    public RabbitMQConfig(RabbitMQProperties properties, AmqpAdmin amqpAdmin) {
        this.properties = properties;
        this.amqpAdmin = amqpAdmin;
    }

   @PostConstruct
    public void declareQueuesAndExchanges() {
       try {
           declareExchanges();
           declareQueues();
           declareBindings();
       } catch (Exception e) {
           logger.error("Failed to declare RabbitMQ components. Will retry later.", e);
       }
    }

    private void declareExchanges() {
        for (RabbitMQProperties.ExchangeConfig exchangeConfig : properties.getExchanges()) {
            Exchange exchange;
            switch (exchangeConfig.getType().toLowerCase()) {
                case "topic":
                    exchange = new TopicExchange(exchangeConfig.getName(), true, false);
                    break;
                case "direct":
                    exchange = new DirectExchange(exchangeConfig.getName(), true, false);
                    break;
                case "fanout":
                    exchange = new FanoutExchange(exchangeConfig.getName(), true, false);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported exchange type: " + exchangeConfig.getType());
            }
            amqpAdmin.declareExchange(exchange);
            logger.info("Declared exchange: {} of type {}", exchangeConfig.getName(), exchangeConfig.getType());
        }
    }

    private void declareQueues() {
        for (RabbitMQProperties.QueueConfig queueConfig : properties.getQueues()) {
            Queue queue = new Queue(queueConfig.getName(), queueConfig.isDurable());
            amqpAdmin.declareQueue(queue);
            logger.info("Declared queue: {}", queueConfig.getName());
        }
    }

    private void declareBindings() {
        for (RabbitMQProperties.BindingConfig bindingConfig : properties.getBindings()) {
            Queue queue = new Queue(bindingConfig.getQueue());
            Exchange exchange = getExchangeByName(bindingConfig.getExchange());
            Binding binding = BindingBuilder.bind(queue)
                    .to(exchange)
                    .with(bindingConfig.getRoutingKey())
                    .noargs();
            amqpAdmin.declareBinding(binding);
            logger.info("Declared binding: queue {} to exchange {} with routing key {}",
                    bindingConfig.getQueue(), bindingConfig.getExchange(), bindingConfig.getRoutingKey());
        }
    }

    private Exchange getExchangeByName(String exchangeName) {
        for (RabbitMQProperties.ExchangeConfig exchangeConfig : properties.getExchanges()) {
            if (exchangeConfig.getName().equals(exchangeName)) {
                switch (exchangeConfig.getType().toLowerCase()) {
                    case "topic":
                        return new TopicExchange(exchangeConfig.getName(), true, false);
                    case "direct":
                        return new DirectExchange(exchangeConfig.getName(), true, false);
                    case "fanout":
                        return new FanoutExchange(exchangeConfig.getName(), true, false);
                    default:
                        throw new IllegalArgumentException("Unsupported exchange type: " + exchangeConfig.getType());
                }
            }
        }
        throw new IllegalArgumentException("Exchange not found: " + exchangeName);
    }

    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter);
        return rabbitTemplate;
    }
}