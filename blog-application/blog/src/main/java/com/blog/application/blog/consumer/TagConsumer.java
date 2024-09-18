package com.blog.application.blog.consumer;

import com.blog.application.blog.dtos.common.PostDto;
import com.blog.application.blog.dtos.common.TagDto;
import com.blog.application.blog.entities.Post;
import com.blog.application.blog.entities.Tag;
import com.blog.application.blog.repositories.PostRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;


@Service
public class TagConsumer {

    private final PostRepository postRepository;
    private static final Logger logger = LogManager.getLogger(TagConsumer.class);
    private static final String ADD_TAG_QUEUE = "ADD_TAG_QUEUE";

    public TagConsumer(PostRepository postRepository) {
        this.postRepository = postRepository;
    }


    @RabbitListener(queues = ADD_TAG_QUEUE)
    public void receiveMessage(PostDto postDto) {
        try {
            Post post = convertToEntity(postDto);
            postRepository.save(post);
            logger.info("Post is saved by ADD_TAG_QUEUE: {}", post);
        } catch (Exception e) {
            logger.error("Error saving post from ADD_TAG_QUEUE: {}", postDto, e);
        }
    }

    private Post convertToEntity(PostDto dto) {
        Post post = postRepository.findById(dto.getPostId()).orElse(new Post());
        post.setTags(dto.getTags().stream().map(this::convertToTagEntity).collect(Collectors.toSet()));
        return post;
    }

    private Tag convertToTagEntity(TagDto dto) {
        Tag newTag = new Tag();
        newTag.setName(dto.getName());
        return newTag;

    }
}