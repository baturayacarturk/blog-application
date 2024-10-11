package com.blog.application.blog.service;

import com.blog.application.blog.dtos.common.TagDto;
import com.blog.application.blog.dtos.responses.post.AddTagResponse;
import com.blog.application.blog.entities.Post;
import com.blog.application.blog.entities.Tag;
import com.blog.application.blog.exceptions.types.BusinessException;
import com.blog.application.blog.repositories.TagRepository;
import com.blog.application.blog.services.post.PostService;
import com.blog.application.blog.services.tag.TagServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;

import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
@ActiveProfiles("test")

public class TagServiceImplTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private PostService postService;

    @InjectMocks
    private TagServiceImpl tagService;

    private Post mockPost;
    private Tag mockTag;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockPost = new Post();
        mockPost.setId(1L);

        mockTag = new Tag();
        mockTag.setId(1L);
        mockTag.setName("Tag to Remove");
    }

    @Test
    @Transactional
    public void testAddTagToPost() {
        TagDto tagDto = new TagDto();
        tagDto.setName("New Tag");

        Tag tag = new Tag();
        tag.setName(tagDto.getName());

        when(postService.getPostEntity(anyLong())).thenReturn(mockPost);
        when(tagRepository.save(any(Tag.class))).thenReturn(tag);
        when(postService.addTagToPost(anyLong(), any(Tag.class))).thenReturn(mockPost);

        AddTagResponse result = tagService.addTagToPost(1L, tagDto);

        assertNotNull(result);
        assertEquals("New Tag", result.getName());
        assertEquals(1L, result.getPostId());
        verify(tagRepository, times(1)).save(any(Tag.class));
        verify(postService, times(1)).addTagToPost(anyLong(), any(Tag.class));
    }

    @Test
    @Transactional
    public void testRemoveTag() {
        Tag tag = new Tag();
        tag.setId(1L);
        tag.setName("Tag to Remove");

        Post mockPost = new Post();
        mockPost.setId(1L);
        mockPost.setTags(new HashSet<>(List.of(tag)));

        when(postService.getPostEntity(anyLong())).thenReturn(mockPost);
        when(tagRepository.findById(anyLong())).thenReturn(Optional.of(tag));

        Post updatedPost = new Post();
        updatedPost.setId(1L);
        updatedPost.setTags(new HashSet<>());
        when(postService.removeTagFromPost(any(Post.class))).thenReturn(updatedPost);

        TagDto result = tagService.removeTag(1L, 1L);

        assertNotNull(result);
        assertEquals("Tag to Remove", result.getName());
        verify(postService, times(1)).removeTagFromPost(any(Post.class));
    }

    @Test
    public void testRemoveTag_TagNotFound() {
        when(postService.getPostEntity(anyLong())).thenReturn(mockPost);

        assertThrows(BusinessException.class, () -> tagService.removeTag(1L, 99L));
    }

}
