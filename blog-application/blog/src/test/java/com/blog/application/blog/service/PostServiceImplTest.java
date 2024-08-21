package com.blog.application.blog.service;

import com.blog.application.blog.dtos.common.SimplifiedPost;
import com.blog.application.blog.dtos.common.TagDto;
import com.blog.application.blog.dtos.requests.post.UpdatePostRequest;
import com.blog.application.blog.dtos.responses.post.CreatedSimpleBlogPost;
import com.blog.application.blog.dtos.requests.post.CreatePostRequest;
import com.blog.application.blog.dtos.responses.post.GetAllByTagId;
import com.blog.application.blog.dtos.responses.post.GetAllSimplifiedPost;
import com.blog.application.blog.dtos.responses.post.UpdatedPostResponse;
import com.blog.application.blog.entities.Post;
import com.blog.application.blog.entities.Tag;
import com.blog.application.blog.projection.SimplifiedPostProjection;
import com.blog.application.blog.repositories.PostRepository;
import com.blog.application.blog.repositories.TagRepository;
import com.blog.application.blog.services.post.PostServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class PostServiceImplTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private PostServiceImpl postService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateBlogPost() {
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("Test Title");
        request.setText("Test Text");
        request.setUserId(1L);
        request.setTags(Arrays.asList(new TagDto("Tag1"), new TagDto("Tag2")));

        Post post = new Post();
        post.setTitle(request.getTitle());
        post.setText(request.getText());
        post.setId(request.getUserId());
        post.setTags(request.getTags().stream()
                .map(tagDto -> {
                    Tag tag = new Tag();
                    tag.setName(tagDto.getName());
                    return tag;
                })
                .collect(Collectors.toSet()));

        when(postRepository.save(any(Post.class))).thenReturn(post);
        when(tagRepository.saveAll(any(Collection.class))).thenReturn(new ArrayList<>());

        CreatedSimpleBlogPost result = postService.createBlogPost(request);

        assertNotNull(result);
        assertEquals("Test Title", result.getTitle());
        assertEquals("Test Text", result.getText());
    }

    @Test
    public void testGetAllSimplifiedPosts() {
        SimplifiedPostProjection projection = mock(SimplifiedPostProjection.class);
        when(projection.getTitle()).thenReturn("Title");
        when(projection.getText()).thenReturn("Text");

        List<SimplifiedPostProjection> projections = Collections.singletonList(projection);
        when(postRepository.getAllSimplifiedBlogPost()).thenReturn(projections);

        GetAllSimplifiedPost result = postService.getAllSimplifiedPosts();

        assertNotNull(result);
        assertEquals(1, result.getPosts().size());
        SimplifiedPost simplifiedPost = result.getPosts().get(0);
        assertEquals("Title", simplifiedPost.getTitle());
        assertEquals("Text", simplifiedPost.getText());
    }

    @Test
    public void testAddTagToPost() {
        Post post = new Post();
        Tag tag = new Tag();
        tag.setName("New Tag");

        when(postRepository.getPostEntity(anyLong())).thenReturn(post);
        when(postRepository.save(any(Post.class))).thenReturn(post);

        Post result = postService.addTagToPost(1L, tag);

        assertNotNull(result);
        assertTrue(result.getTags().contains(tag));
    }

    @Test
    public void testRemoveTagFromPost() {
        Post post = new Post();
        Tag tag = new Tag();
        tag.setName("Tag to Remove");
        post.getTags().add(tag);

        when(postRepository.save(any(Post.class))).thenReturn(post);

        Post result = postService.removeTagFromPost(post);

        assertNotNull(result);
    }

    @Test
    public void testGetPostEntity() {
        Post post = new Post();
        when(postRepository.getPostEntity(anyLong())).thenReturn(post);

        Post result = postService.getPostEntity(1L);

        assertNotNull(result);
    }

    @Test
    public void testGetPostsByTagId() {
        Tag tag = new Tag();
        Post post = new Post();
        post.getTags().add(tag);

        when(postRepository.getPostEntityByTagId(anyLong())).thenReturn(Collections.singletonList(post));

        List<GetAllByTagId> result = postService.getPostsByTagId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        GetAllByTagId getAllByTagId = result.get(0);
        assertEquals(post.getTitle(), getAllByTagId.getTitle());
        assertEquals(post.getText(), getAllByTagId.getText());
        assertEquals(1, getAllByTagId.getTags().size());
    }

    @Test
    public void testUpdatePost() {
        UpdatePostRequest request = new UpdatePostRequest();
        request.setId(1L);
        request.setTitle("Updated Title");
        request.setText("Updated Text");

        Post post = new Post();
        post.setId(1L);
        post.setTitle("Old Title");
        post.setText("Old Text");

        when(postRepository.getPostEntity(anyLong())).thenReturn(post);
        when(postRepository.save(any(Post.class))).thenReturn(post);

        UpdatedPostResponse result = postService.updatePost(request);

        assertNotNull(result);
        assertEquals(1L, result.getPostId());
        assertEquals("Updated Title", result.getTitle());
        assertEquals("Updated Text", result.getText());
    }
}
