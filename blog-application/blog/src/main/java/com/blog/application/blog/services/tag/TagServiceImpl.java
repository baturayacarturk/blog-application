package com.blog.application.blog.services.tag;

import com.blog.application.blog.dtos.common.TagDto;
import com.blog.application.blog.dtos.responses.post.AddTagResponse;
import com.blog.application.blog.entities.Post;
import com.blog.application.blog.entities.Tag;
import com.blog.application.blog.entities.User;
import com.blog.application.blog.exceptions.messages.TagExceptionMessages;
import com.blog.application.blog.exceptions.types.BusinessException;
import com.blog.application.blog.repositories.TagRepository;
import com.blog.application.blog.services.post.PostService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Optional;

@Service
public class TagServiceImpl implements TagService {
    private static final Logger logger = LogManager.getLogger(TagServiceImpl.class);

    private final TagRepository tagRepository;
    private final PostService postService;

    public TagServiceImpl(TagRepository tagRepository, PostService postService) {
        this.tagRepository = tagRepository;
        this.postService = postService;
    }

    @Override
    @Transactional
    public AddTagResponse addTagToPost(Long postId, TagDto tagDto) {
        logger.info("Adding tag to post. Post ID: {}, Tag Name: {}", postId, tagDto.getName());
        Tag tag = new Tag();
        Post post = postService.getPostEntity(postId);
        if(post == null){
            String errorMessage = "Post could not be found with ID: " + postId;
            logger.error(errorMessage);
            throw new BusinessException(errorMessage);
        }
        tag.setName(tagDto.getName());
        tagRepository.save(tag);
        postService.addTagToPost(postId, tag);
        AddTagResponse response = new AddTagResponse();
        response.setName(tagDto.getName());
        response.setPostId(postId);
        logger.info("Tag successfully added to post. Post ID: {}, Tag Name: {}", postId, tagDto.getName());
        return response;
    }

    @Override
    public TagDto removeTag(Long postId, Long tagId) {
        logger.info("Removing tag from post. Post ID: {}, Tag ID: {}", postId, tagId);
        Post post = postService.getPostEntity(postId);
        if(post == null){
            String errorMessage = "Post could not be found with ID: " + postId;
            logger.error(errorMessage);
            throw new BusinessException(errorMessage);
        }
        Optional<Tag> tagToRemove = post.getTags().stream().filter(tag -> tag.getId().equals(tagId)).findFirst();
        if (tagToRemove.isEmpty()) {
            String errorMessage = String.format(TagExceptionMessages.TAG_COULD_NOT_FOUND, tagId);
            logger.error(errorMessage);
            throw new BusinessException(errorMessage);
        }
        post.getTags().remove(tagToRemove.get());
        postService.removeTagFromPost(post);
        TagDto tagDto = new TagDto();
        tagDto.setName(tagToRemove.get().getName());
        logger.info("Tag successfully removed from post. Post ID: {}, Tag ID: {}", postId, tagId);
        return tagDto;
    }

    private static User extractUserNameFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (principal instanceof User) {
            return (User) principal;
        } else if (principal instanceof Optional<?>) {
            Optional<?> optionalPrincipal = (Optional<?>) principal;
            if (optionalPrincipal.isPresent() && optionalPrincipal.get() instanceof User) {
                return (User) optionalPrincipal.get();
            } else {
                throw new IllegalStateException("Unexpected principal type");
            }
        } else {
            throw new IllegalStateException("Principal is not of type User or Optional<User>");
        }
    }
}
