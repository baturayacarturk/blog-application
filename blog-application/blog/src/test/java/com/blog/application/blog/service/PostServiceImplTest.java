package com.blog.application.blog.service;

import com.blog.application.blog.dtos.common.TagDto;
import com.blog.application.blog.dtos.requests.post.CreatePostRequest;
import com.blog.application.blog.dtos.requests.post.UpdatePostRequest;
import com.blog.application.blog.dtos.responses.client.UserClientDto;
import com.blog.application.blog.dtos.responses.post.*;
import com.blog.application.blog.entities.Post;
import com.blog.application.blog.entities.Tag;
import com.blog.application.blog.entities.elastic.ElasticPost;
import com.blog.application.blog.exceptions.types.BusinessException;
import com.blog.application.blog.projection.SimplifiedPostProjection;
import com.blog.application.blog.producer.ElasticEventsProducer;
import com.blog.application.blog.producer.TagProducer;
import com.blog.application.blog.repositories.PostRepository;
import com.blog.application.blog.repositories.TagRepository;
import com.blog.application.blog.repositories.elastic.PostElasticRepository;
import com.blog.application.blog.services.client.UserFeignClient;
import com.blog.application.blog.services.post.PostServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
public class PostServiceImplTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private PostElasticRepository postElasticRepository;

    @Mock
    private UserFeignClient userFeignClient;

    @Mock
    private ElasticEventsProducer elasticEventsProducer;

    @Mock
    private TagProducer tagProducer;

    @InjectMocks
    private PostServiceImpl postService;

    private Post mockPost;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockPost = new Post();
        mockPost.setId(1L);
        mockPost.setUserId(10L);
        UserClientDto mockUserClientDto = new UserClientDto();
        mockUserClientDto.setId(10L);

        when(userFeignClient.getUserDetails()).thenReturn(ResponseEntity.ok(mockUserClientDto));
    }

    @Test
    public void testCreateBlogPost() {
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("Test Title");
        request.setText("Test Text");
        request.setUserId(10L);
        request.setTags(Arrays.asList(new TagDto("Tag1"), new TagDto("Tag2")));

        Post post = new Post();
        post.setId(1L);
        post.setTitle(request.getTitle());
        post.setText(request.getText());
        post.setUserId(10L);
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
        verify(elasticEventsProducer, times(1)).sendElasticEvent(any());
    }


    @Test
    public void testGetAllSimplifiedPosts() {
        SimplifiedPostProjection projection = mock(SimplifiedPostProjection.class);
        when(projection.getTitle()).thenReturn("Title");
        when(projection.getText()).thenReturn("Text");
        Pageable pageable = PageRequest.of(0, 3);

        List<SimplifiedPostProjection> projections = Collections.singletonList(projection);
        Page<SimplifiedPostProjection> page = new PageImpl<>(projections, pageable, projections.size());

        when(postRepository.getAllSimplifiedBlogPost(pageable)).thenReturn(page);
        GetAllSimplifiedPost resultPage = postService.getAllSimplifiedPosts(0, 3);


        assertNotNull(resultPage);
        assertEquals(1, resultPage.getPosts().size());

        var simplifiedPost = resultPage.getPosts().get(0);
        assertEquals("Title", simplifiedPost.getTitle());
        assertEquals("Text", simplifiedPost.getText());
    }

    @Test
    public void testAddTagToPost() {
        Post post = new Post();
        post.setUserId(10L);
        Tag tag = new Tag();
        tag.setName("New Tag");

        when(postRepository.getPostEntity(anyLong())).thenReturn(post);
        when(postRepository.save(any(Post.class))).thenReturn(post);

        Post result = postService.addTagToPost(1L, tag);

        assertNotNull(result);
        assertTrue(result.getTags().contains(tag));
        verify(tagProducer, times(1)).sendMessage(any(Post.class));
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
        verify(postRepository, times(1)).save(post);
    }

    @Test
    public void testGetPostEntity() {
        when(postRepository.getPostEntity(anyLong())).thenReturn(mockPost);

        Post result = postService.getPostEntity(1L);

        assertNotNull(result);
        assertEquals(mockPost.getId(), result.getId());
    }

    @Test
    public void testGetPostsByTagId() {
        Tag tag = new Tag();
        Post post = new Post();
        post.setUserId(10L);
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
        post.setUserId(10L);

        when(postRepository.getPostEntity(anyLong())).thenReturn(post);
        when(postRepository.save(any(Post.class))).thenReturn(post);

        UpdatedPostResponse result = postService.updatePost(request);

        assertNotNull(result);
        assertEquals(1L, result.getPostId());
        assertEquals("Updated Title", result.getTitle());
        assertEquals("Updated Text", result.getText());
    }

    @Test
    public void testDeletePostById_PostNotFound() {
        when(postRepository.getPostEntity(anyLong())).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            postService.deletePostById(1L);
        });

        assertEquals("Post not found", exception.getMessage());
    }

    @Test
    public void testDeletePostById_UserDoesNotOwnPost() {
        when(postRepository.getPostEntity(anyLong())).thenReturn(mockPost);
        UserClientDto differentUser = new UserClientDto();
        differentUser.setId(20L);
        when(userFeignClient.getUserDetails()).thenReturn(ResponseEntity.ok(differentUser));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            postService.deletePostById(1L);
        });

        assertEquals("You are accessing a resource that you are not permitted", exception.getMessage());
    }

    @Test
    public void testDeletePostById_Success() {
        when(postRepository.getPostEntity(anyLong())).thenReturn(mockPost);
        when(postRepository.save(any(Post.class))).thenReturn(mockPost);
        UserClientDto userClientDto = new UserClientDto();
        userClientDto.setId(10L);
        when(userFeignClient.getUserDetails()).thenReturn(ResponseEntity.ok(userClientDto));

        DeletedPostResponse response = postService.deletePostById(1L);

        assertNotNull(response);
    }
}
