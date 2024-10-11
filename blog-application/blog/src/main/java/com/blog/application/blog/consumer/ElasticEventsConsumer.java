package com.blog.application.blog.consumer;

import com.blog.application.blog.dtos.common.ElasticPostEvent;
import com.blog.application.blog.entities.elastic.ElasticPost;
import com.blog.application.blog.repositories.elastic.PostElasticRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ElasticEventsConsumer {
    private final PostElasticRepository elasticRepository;
    private final ObjectMapper objectMapper;

    private static final Logger logger = LogManager.getLogger(ElasticEventsConsumer.class);

    public ElasticEventsConsumer(PostElasticRepository elasticRepository, ObjectMapper objectMapper) {
        this.elasticRepository = elasticRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = {"elastic-events"}, groupId = "elastic-events")
    public void onMessage(ConsumerRecord<Long, String> consumerRecord) {
        try {
            ElasticPostEvent elasticPostEvent = objectMapper.readValue(consumerRecord.value(), ElasticPostEvent.class);
            if (elasticPostEvent.text() == null || elasticPostEvent.text().isEmpty()) {
                throw new RecoverableDataAccessException("Just testing purpose");
            }
            ElasticPost elasticPost = new ElasticPost();
            elasticPost.setId(elasticPostEvent.eventId());
            elasticPost.setUserId(elasticPostEvent.userId());
            elasticPost.setText(elasticPostEvent.text());
            elasticPost.setTitle(elasticPostEvent.title());
            elasticPost.setTags(elasticPostEvent.tags());

            elasticRepository.save(elasticPost);
            logger.info("ElasticPost saved successfully: {}", elasticPost.getId());
        } catch (JsonProcessingException e) {
            logger.error("Error processing ConsumerRecord: {}", consumerRecord, e);
        }
    }

}


