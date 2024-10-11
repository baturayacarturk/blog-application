package com.blog.application.blog.producer;

import com.blog.application.blog.config.RabbitMQProperties;
import com.blog.application.blog.dtos.common.PostDto;
import com.blog.application.blog.dtos.common.TagDto;
import com.blog.application.blog.entities.Post;
import com.blog.application.blog.entities.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TagProducer {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQProperties properties;

    private String tagExchange;
    private String tagRoutingKey;

    private static final String TAG_QUEUE = "ADD_TAG_QUEUE";
    private static final Logger logger = LogManager.getLogger(TagProducer.class);

    public TagProducer(RabbitTemplate rabbitTemplate, RabbitMQProperties properties) {
        this.rabbitTemplate = rabbitTemplate;
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        logger.info("TagProducer initialized");

        List<RabbitMQProperties.BindingConfig> bindings = properties.getBindings();
        RabbitMQProperties.BindingConfig tagBindingConfig =
                bindings.stream()
                        .filter(binding -> binding.getQueue().equals(TAG_QUEUE))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("No binding found for " + TAG_QUEUE));

        this.tagExchange = tagBindingConfig.getExchange();
        this.tagRoutingKey = tagBindingConfig.getRoutingKey();

        logger.info("TagProducer configured with exchange: {}, routing key: {}", tagExchange, tagRoutingKey);
    }

    public void sendMessage(Post post) {
        if (post != null) {
            PostDto postDTO = convertToDTO(post);

            rabbitTemplate.convertAndSend(tagExchange, tagRoutingKey, postDTO);
            logger.info("Sent message for post: {} with tag: {}", post.getId());
        }
    }

    private PostDto convertToDTO(Post post) {
        PostDto dto = new PostDto();
        dto.setPostId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setText(post.getText());
        dto.setTags(post.getTags().stream().map(this::convertToTagDTO).collect(Collectors.toList()));
        return dto;
    }

    private TagDto convertToTagDTO(Tag tag) {
        TagDto dto = new TagDto();
        dto.setName(tag.getName());
        return dto;
    }
}