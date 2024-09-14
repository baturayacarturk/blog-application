package com.blog.application.blog.producer;

import com.blog.application.blog.dtos.common.ElasticPostEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class ElasticEventsProducer {

    @Value("${spring.kafka.topic}")
    public String topic;
    private final KafkaTemplate<Long, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private static final Logger logger = LogManager.getLogger(ElasticEventsProducer.class);


    public ElasticEventsProducer(KafkaTemplate<Long, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public CompletableFuture<SendResult<Long, String>> sendElasticEvent(ElasticPostEvent elasticPostEvent)  {
        var key = elasticPostEvent.eventId();
        String value;
        try {
            value = objectMapper.writeValueAsString(elasticPostEvent);
        } catch (JsonProcessingException e) {
            logger.error("Error while serializing ElasticPostEvent", e);
            return CompletableFuture.failedFuture(e);
        }
        var producerRecord = buildProducerRecord(key,value);
        var listenableFuture = kafkaTemplate.send(producerRecord);

        return listenableFuture.completable().whenComplete((sendResult, throwable) -> {
            if (throwable != null) {
                handleFailure(key, value, throwable);
            } else {
                handleSuccess(key, value, sendResult);

            }
        });
    }

    private ProducerRecord<Long, String> buildProducerRecord(Long key, String value) {
        return new ProducerRecord<>(topic,key,value);
    }

    private void handleSuccess(Long key, String value, SendResult<Long, String> sendResult) {
        logger.info("Message send successfully for key: {} and the value: {}, partition: {} ", key, value,
                sendResult.getRecordMetadata().partition());
    }

    private void handleFailure(Long key, String value, Throwable throwable) {
        logger.error("Error while sending message and exception is : {} ", throwable.getMessage(), throwable);
    }

}
