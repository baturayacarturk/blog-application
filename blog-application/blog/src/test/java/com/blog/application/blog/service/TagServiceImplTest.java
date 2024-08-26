package com.blog.application.blog.service;

import com.blog.application.blog.dtos.common.TagDto;
import com.blog.application.blog.dtos.responses.post.AddTagResponse;
import com.blog.application.blog.entities.Post;
import com.blog.application.blog.entities.Tag;
import com.blog.application.blog.exceptions.messages.TagExceptionMessages;
import com.blog.application.blog.exceptions.types.BusinessException;
import com.blog.application.blog.repositories.TagRepository;
import com.blog.application.blog.services.post.PostService;
import com.blog.application.blog.services.tag.TagServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class TagServiceImplTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private PostService postService;

    @InjectMocks
    private TagServiceImpl tagService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testAddTagToPost() {
        Long postId = 1L;
        TagDto tagDto = new TagDto();
        tagDto.setName("New Tag");

        Tag tag = new Tag();
        tag.setName(tagDto.getName());

        when(tagRepository.save(any(Tag.class))).thenReturn(tag);
        Post expectedPost = new Post();
        when(postService.addTagToPost(eq(postId), any(Tag.class))).thenReturn(expectedPost);

        AddTagResponse response = tagService.addTagToPost(postId, tagDto);

        assertNotNull(response);
        assertEquals("New Tag", response.getName());
        assertEquals(postId, response.getPostId());
        verify(tagRepository).save(any(Tag.class));
        verify(postService).addTagToPost(eq(postId), any(Tag.class));
    }

    @Test
    public void testRemoveTag() {
        Long postId = 1L;
        Long tagId = 1L;

        Post post = new Post();
        Tag tag = new Tag();
        tag.setId(tagId);
        tag.setName("Tag to Remove");
        post.setTags(new HashSet<>());
        post.getTags().add(tag);

        when(postService.getPostEntity(postId)).thenReturn(post);
        Post expectedPost = new Post();
        when(postService.removeTagFromPost(post)).thenReturn(expectedPost);

        TagDto result = tagService.removeTag(postId, tagId);

        assertNotNull(result);
        assertEquals("Tag to Remove", result.getName());
        assertFalse(post.getTags().contains(tag));
        verify(postService).getPostEntity(postId);
        verify(postService).removeTagFromPost(post);
    }

    @Test
    public void testRemoveTagNotFound() {
        Long postId = 1L;
        Long tagId = 1L;

        Post post = new Post();
        post.setTags(new HashSet<>());

        when(postService.getPostEntity(postId)).thenReturn(post);

        BusinessException thrown = assertThrows(BusinessException.class, () -> {
            tagService.removeTag(postId, tagId);
        });

        assertEquals(String.format(TagExceptionMessages.TAG_COULD_NOT_FOUND, tagId), thrown.getMessage());
        verify(postService).getPostEntity(postId);
        verify(postService, never()).removeTagFromPost(any(Post.class));
    }
}
