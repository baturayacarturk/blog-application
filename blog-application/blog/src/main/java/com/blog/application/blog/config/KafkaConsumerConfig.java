package com.blog.application.blog.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

@Configuration
@EnableKafka
public class KafkaConsumerConfig {
    private static final Logger logger = LogManager.getLogger(KafkaConsumerConfig.class);

    @Bean
    ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ConsumerFactory<Object, Object> kafkaConsumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        configurer.configure(factory, kafkaConsumerFactory);
        factory.setCommonErrorHandler(errorHandler());
        return factory;

    }

    public DefaultErrorHandler errorHandler() {
        //retry failed record twice with one second delay.
        //var fixedBackoff = new FixedBackOff(1000L, 2);
        var exponentialBackOff = new ExponentialBackOffWithMaxRetries(3);
        exponentialBackOff.setInitialInterval(1_000L);
        exponentialBackOff.setMultiplier(2.0);
        exponentialBackOff.setMaxInterval(6_000L);
        var errorHandler = new DefaultErrorHandler(exponentialBackOff);
        errorHandler.setRetryListeners(((consumerRecord, e, i) ->
        {
            logger.info("Failed record in retry listener :{} , attempt : {}", e.getMessage(), i);
        }));

        return errorHandler;
    }
}
