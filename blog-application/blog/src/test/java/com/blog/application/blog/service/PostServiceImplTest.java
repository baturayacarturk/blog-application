package com.blog.application.blog.service;

import com.blog.application.blog.dtos.common.SimplifiedPost;
import com.blog.application.blog.dtos.common.TagDto;
import com.blog.application.blog.dtos.requests.post.UpdatePostRequest;
import com.blog.application.blog.dtos.responses.post.*;
import com.blog.application.blog.dtos.requests.post.CreatePostRequest;
import com.blog.application.blog.entities.Post;
import com.blog.application.blog.entities.Tag;
import com.blog.application.blog.exceptions.types.BusinessException;
import com.blog.application.blog.projection.SimplifiedPostProjection;
import com.blog.application.blog.repositories.PostRepository;
import com.blog.application.blog.repositories.TagRepository;
import com.blog.application.blog.repositories.UserRepository;
import com.blog.application.blog.services.post.PostServiceImpl;
import com.blog.application.blog.services.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import com.blog.application.blog.entities.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;


public class PostServiceImplTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private TagRepository tagRepository;
    @Mock
    private UserService userService;

    @InjectMocks
    private PostServiceImpl postService;

    @Mock
    private UserRepository userRepository;
    private User mockUser;
    private Post mockPost;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testUser");
        userRepository.save(mockUser);

        mockPost = new Post();
        mockPost.setId(1L);
        mockPost.setUser(mockUser);

        Authentication authentication = Mockito.mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(mockUser);

        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);
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
        post.setUser(mockUser);
        post.setTags(request.getTags().stream()
                .map(tagDto -> {
                    Tag tag = new Tag();
                    tag.setName(tagDto.getName());
                    return tag;
                })
                .collect(Collectors.toSet()));

        when(postRepository.save(any(Post.class))).thenReturn(post);
        when(tagRepository.saveAll(any(Collection.class))).thenReturn(new ArrayList<>());
        when(userService.findByUsername(mockUser.getUsername())).thenReturn(Optional.of(mockUser));

        CreatedSimpleBlogPost result = postService.createBlogPost(request);

        assertNotNull(result);
        assertEquals("Test Title", result.getTitle());
        assertEquals("Test Text", result.getText());
    }
    @Test
    public void testCreateBlogPost_UserNotFound() {
        CreatePostRequest createPostRequest = new CreatePostRequest();
        createPostRequest.setTitle("Test Title");
        createPostRequest.setText("Test Text");
        createPostRequest.setUserId(1L);
        createPostRequest.setTags(Arrays.asList(new TagDto("Tag1"), new TagDto("Tag2")));

        when(userService.findByUsername(anyString())).thenReturn(Optional.empty());

        BusinessException thrown = assertThrows(BusinessException.class, () -> {
            postService.createBlogPost(createPostRequest);
        });
        assertEquals("User not found", thrown.getMessage());
    }
    @Test
    public void testCreateBlogPost_UnauthorizedAccess() {
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testUser");

        CreatePostRequest createPostRequest = new CreatePostRequest();
        createPostRequest.setTitle("Test Title");
        createPostRequest.setText("Test Text");
        createPostRequest.setUserId(2L);
        createPostRequest.setTags(Arrays.asList(new TagDto("Tag1"), new TagDto("Tag2")));

        when(userService.findByUsername(anyString())).thenReturn(Optional.of(mockUser));
        BusinessException thrown = assertThrows(BusinessException.class, () -> {
            postService.createBlogPost(createPostRequest);
        });
        assertEquals("You are accessing a resource that you are not permitted", thrown.getMessage());
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
        post.setUser(mockUser);
        Tag tag = new Tag();
        tag.setName("New Tag");
        when(userService.findByUsername(mockUser.getUsername())).thenReturn(Optional.of(mockUser));
        when(postRepository.getPostEntity(anyLong())).thenReturn(post);
        when(postRepository.save(any(Post.class))).thenReturn(post);


        Post result = postService.addTagToPost(1L, tag);

        assertNotNull(result);
        assertTrue(result.getTags().contains(tag));
    }
    @Test
    public void testAddTagToPost_UnauthorizedAccess() {
        Long postId = 1L;
        Tag newTag = new Tag();
        newTag.setName("New Tag");

        Post existingPost = new Post();
        existingPost.setId(postId);
        existingPost.setTitle("Existing Title");
        existingPost.setText("Existing Text");
        existingPost.setTags(new HashSet<>());

        User mockUser = new User();
        mockUser.setUsername("testUser");

        User postOwner = new User();
        postOwner.setUsername("otherUser");

        existingPost.setUser(postOwner);
        Authentication authentication = Mockito.mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(mockUser);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(postRepository.getPostEntity(postId)).thenReturn(existingPost);
        BusinessException thrown = assertThrows(BusinessException.class, () -> {
            postService.addTagToPost(postId, newTag);
        });
        assertEquals("User not found", thrown.getMessage());
        verify(postRepository, never()).save(any(Post.class));
        verify(postRepository, times(1)).getPostEntity(postId);
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
        post.setUser(mockUser);
        post.getTags().add(tag);

        when(postRepository.getPostEntityByTagId(anyLong())).thenReturn(Collections.singletonList(post));
        when(userService.findByUsername(mockUser.getUsername())).thenReturn(Optional.of(mockUser));

        List<GetAllByTagId> result = postService.getPostsByTagId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        GetAllByTagId getAllByTagId = result.get(0);
        assertEquals(post.getTitle(), getAllByTagId.getTitle());
        assertEquals(post.getText(), getAllByTagId.getText());
        assertEquals(1, getAllByTagId.getTags().size());
    }
    @Test
    public void testGetPostsByTagId_UnauthorizedAccess() {
        Long tagId = 1L;
        Long userId = 1L;

        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("otherUser");
        Post post = new Post();
        post.setId(1L);
        post.setTitle("Post Title");
        post.setText("Post Text");
        post.setUser(otherUser);

        List<Post> postList = Collections.singletonList(post);

        Authentication authentication = Mockito.mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(mockUser);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(postRepository.getPostEntityByTagId(tagId)).thenReturn(postList);

        BusinessException thrown = assertThrows(BusinessException.class, () -> {
            postService.getPostsByTagId(tagId);
        });
        assertEquals("You are accessing a resource that you are not permitted", thrown.getMessage());
        verify(postRepository, times(1)).getPostEntityByTagId(tagId);
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
        post.setUser(mockUser);

        when(postRepository.getPostEntity(anyLong())).thenReturn(post);
        when(postRepository.save(any(Post.class))).thenReturn(post);
        when(userService.findByUsername(mockUser.getUsername())).thenReturn(Optional.of(mockUser));


        UpdatedPostResponse result = postService.updatePost(request);

        assertNotNull(result);
        assertEquals(1L, result.getPostId());
        assertEquals("Updated Title", result.getTitle());
        assertEquals("Updated Text", result.getText());
    }
    @Test
    public void testUpdatePost_UnauthorizedAccess() {
        Long postId = 1L;
        Long userId = 1L;

        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("otherUser");

        Post existingPost = new Post();
        existingPost.setId(postId);
        existingPost.setTitle("Old Title");
        existingPost.setText("Old Text");
        existingPost.setUser(otherUser);

        UpdatePostRequest updateRequest = new UpdatePostRequest();
        updateRequest.setId(postId);
        updateRequest.setTitle("Updated Title");
        updateRequest.setText("Updated Text");

        Authentication authentication = Mockito.mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(mockUser);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(postRepository.getPostEntity(postId)).thenReturn(existingPost);

        BusinessException thrown = assertThrows(BusinessException.class, () -> {
            postService.updatePost(updateRequest);
        });
        assertEquals("You are accessing a resource that you are not permitted", thrown.getMessage());
        verify(postRepository, times(1)).getPostEntity(postId);
        verify(postRepository, times(0)).save(any(Post.class)); // Ensure save was not called
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
    public void testDeletePostById_UserNotFound() {
        when(postRepository.getPostEntity(anyLong())).thenReturn(mockPost);
        when(userService.findByUsername(anyString())).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            postService.deletePostById(1L);
        });

        assertEquals("User not found", exception.getMessage());
    }
    @Test
    public void testDeletePostById_UserDoesNotOwnPost() {
        User anotherUser = new User();
        anotherUser.setId(2L);
        anotherUser.setUsername("anotherUser");

        mockPost.setUser(anotherUser);

        when(postRepository.getPostEntity(anyLong())).thenReturn(mockPost);
        when(userService.findByUsername(anyString())).thenReturn(Optional.of(mockUser));

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            postService.deletePostById(1L);
        });

        assertEquals("You are accessing a resource that you are not permitted", exception.getMessage());
    }
    @Test
    public void testDeletePostById_Success() {
        mockUser.setPosts(new HashSet<>(Collections.singletonList(mockPost)));

        when(postRepository.getPostEntity(anyLong())).thenReturn(mockPost);
        when(userService.findByUsername(anyString())).thenReturn(Optional.of(mockUser));
        DeletedPostResponse response = postService.deletePostById(1L);

        assertNotNull(response);
        assertEquals("Post with id: 1 is successfully deleted.", response.getResponse());

        verify(postRepository, times(1)).delete(mockPost);

        assertFalse(mockUser.getPosts().contains(mockPost));
        assertTrue(mockPost.getTags().isEmpty());
    }

}
