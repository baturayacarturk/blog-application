package com.blog.application.blog.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "rabbit-config")
@Getter
@Setter
public class RabbitMQProperties {

    private List<ExchangeConfig> exchanges;
    private List<QueueConfig> queues;
    private List<BindingConfig> bindings;

    @Getter
    @Setter
    public static class ExchangeConfig {
        private String name;
        private String type;

    }
    @Getter
    @Setter
    public static class QueueConfig {
        private String name;
        private boolean durable;

    }
    @Getter
    @Setter
    public static class BindingConfig {
        private String queue;
        private String exchange;
        private String routingKey;

    }
}

