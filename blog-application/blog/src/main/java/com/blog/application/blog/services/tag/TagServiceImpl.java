package com.blog.application.blog.services.tag;

import com.blog.application.blog.dtos.common.TagDto;
import com.blog.application.blog.dtos.responses.post.AddTagResponse;
import com.blog.application.blog.entities.Post;
import com.blog.application.blog.entities.Tag;
import com.blog.application.blog.entities.User;
import com.blog.application.blog.exceptions.messages.PostExceptionMessages;
import com.blog.application.blog.exceptions.messages.TagExceptionMessages;
import com.blog.application.blog.exceptions.types.BusinessException;
import com.blog.application.blog.helpers.params.utils.ExtendedStringUtils;
import com.blog.application.blog.repositories.TagRepository;
import com.blog.application.blog.services.post.PostService;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.*;


@Service
public class TagServiceImpl implements TagService {
    private final TagRepository tagRepository;
    private final PostService postService;

    public TagServiceImpl(TagRepository tagRepository, PostService postService) {
        this.tagRepository = tagRepository;
        this.postService = postService;
    }

    @Override
    @Transactional
    public AddTagResponse addTagToPost(Long postId, TagDto tagDto) {
        Tag tag = new Tag();
        User extractedUser = extractUserNameFromSecurityContext();
        Post post = postService.getPostEntity(postId);
        if(post == null){
            throw new BusinessException("Post could not found");
        }
        if(!extractedUser.getId().equals(post.getUser().getId())){
            throw new BusinessException("You are accessing a resource that you are not permitted");
        }
        tag.setName(tagDto.getName());
        tagRepository.save(tag);
        postService.addTagToPost(postId, tag);
        AddTagResponse response = new AddTagResponse();
        response.setName(tagDto.getName());
        response.setPostId(postId);
        return response;
    }

    @Override
    public TagDto removeTag(Long postId, Long tagId) {
        User extractedUser = extractUserNameFromSecurityContext();
        Post post = postService.getPostEntity(postId);
        if(post == null){
            throw new BusinessException("Post could not found");
        }
        if(!extractedUser.getId().equals(post.getUser().getId())){
            throw new BusinessException("You are accessing a resource that you are not permitted");
        }
        Optional<Tag> tagToRemove = post.getTags().stream().filter(tag -> tag.getId().equals(tagId)).findFirst();
        if (tagToRemove.isEmpty()) {
            throw new BusinessException(String.format(TagExceptionMessages.TAG_COULD_NOT_FOUND, tagId));
        }
        post.getTags().remove(tagToRemove.get());
        postService.removeTagFromPost(post);
        TagDto tagDto = new TagDto();
        tagDto.setName(tagToRemove.get().getName());
        return tagDto;
    }
    private static User extractUserNameFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principalUser = authentication.getPrincipal();
        return (User)principalUser;
    }


}
