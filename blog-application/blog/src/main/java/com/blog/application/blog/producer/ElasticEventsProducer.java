package com.blog.application.blog.producer;

import com.blog.application.blog.dtos.common.ElasticPostEvent;
import com.blog.application.blog.exceptions.types.BusinessException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.time.LocalDateTime;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeoutException;

@Component
public class ElasticEventsProducer {

    @Value("${spring.kafka.topic}")
    private String topic;
    private final KafkaTemplate<Long, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private final Queue<ProducerRecord<Long, String>> localEventQueue = new ConcurrentLinkedQueue<>();
    private static final Logger logger = LogManager.getLogger(ElasticEventsProducer.class);

    public ElasticEventsProducer(KafkaTemplate<Long, String> kafkaTemplate,
                                 ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;

    }

    public void sendElasticEvent(ElasticPostEvent elasticPostEvent) {
        var key = elasticPostEvent.eventId();
        String value;
        try {
            value = objectMapper.writeValueAsString(elasticPostEvent);
        } catch (JsonProcessingException e) {
            logger.error("Error while serializing ElasticPostEvent", e);
            throw new BusinessException("Json processing error on ElasticPostEvent");
        }
        var producerRecord = buildProducerRecord(key, value);
        sendToKafka(producerRecord);

    }

    private void sendToKafka(ProducerRecord<Long, String> record) {
        try {
            ListenableFuture<SendResult<Long, String>> future = kafkaTemplate.send(record);
            future.addCallback(new ListenableFutureCallback<SendResult<Long, String>>() {
                @Override
                public void onFailure(Throwable ex) {
                    handleError(record, ex);
                }

                @Override
                public void onSuccess(SendResult<Long, String> result) {
                    retryFailedMessages();

                }
            });
        } catch (Exception e) {
            logger.error("Error ocured while sending message: " + e.getCause());
            handleError(record, e);
        }
    }

    private void handleError(ProducerRecord<Long, String> event, Throwable ex) {
        localEventQueue.offer(event);
        if (ex instanceof TimeoutException) {
            logger.error("TimeoutException occurred while fetching metadata. Message added to queue.: " + LocalDateTime.now());
        } else {
            logger.error("Error occurred. Message added to queue. Error: " + ex.getClass().getName() + "at: " + LocalDateTime.now());
        }
    }

    private ProducerRecord<Long, String> buildProducerRecord(Long key, String value) {
        return new ProducerRecord<>(topic, key, value);
    }


    private void retryFailedMessages() {
        if (!localEventQueue.isEmpty()) {
            logger.info("Attempting to retry failed messages");
            while (!localEventQueue.isEmpty()) {
                ProducerRecord<Long, String> record = localEventQueue.poll();
                if (record != null) {
                    sendToKafka(record);
                }
            }
        }
    }

}