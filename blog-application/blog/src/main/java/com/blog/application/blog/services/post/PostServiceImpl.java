package com.blog.application.blog.services.post;

import com.blog.application.blog.dtos.common.PostDto;
import com.blog.application.blog.dtos.responses.post.CreatedSimpleBlogPost;
import com.blog.application.blog.dtos.requests.post.CreatePostRequest;
import com.blog.application.blog.dtos.responses.post.GetPost;
import com.blog.application.blog.entities.Post;
import com.blog.application.blog.entities.Tag;
import com.blog.application.blog.helpers.params.GetPostsQueryBuilder;
import com.blog.application.blog.repositories.PostRepository;
import com.blog.application.blog.repositories.TagRepository;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PostServiceImpl implements PostService{

    private final PostRepository postRepository;
    private final TagRepository tagRepository;
    private final GetPostsQueryBuilder getPostsQueryBuilder;
    private final EntityManager entityManager;

    public PostServiceImpl(PostRepository postRepository, TagRepository tagRepository, GetPostsQueryBuilder getPostsQueryBuilder, EntityManager entityManager) {
        this.postRepository = postRepository;
        this.tagRepository = tagRepository;
        this.getPostsQueryBuilder = getPostsQueryBuilder;
        this.entityManager = entityManager;
    }

    @Override
    public CreatedSimpleBlogPost createBlogPost(CreatePostRequest createPostRequest) {
        Post post = createdPostRequestToPostEntity(createPostRequest);
        //TODO inject TagService instead repository some business logic can be required.
        //TODO change tag name uniqueness
        List<Tag> tag = tagRepository.saveAll(post.getTags());

        Post savedPost = postRepository.save(post);
        CreatedSimpleBlogPost createdPostDto = new CreatedSimpleBlogPost();
        createdPostDto.setTitle(savedPost.getTitle());
        createdPostDto.setText(savedPost.getText());
        //TODO validate if user null and exists.
        createdPostDto.setUserId(createdPostDto.getUserId());
        createdPostDto.setTags(createPostRequest.getTags());

        return createdPostDto;

    }

    @Override
    public GetPost getPosts() {
        //TODO create post search params when parameters added to endpoint
        String queryString = getPostsQueryBuilder.buildQuery(null);
        Map<String, Object> parameters = getPostsQueryBuilder.getParameters(null);
        TypedQuery<Post> query = entityManager.createQuery(queryString, Post.class);
        parameters.forEach(query::setParameter);
        List<Post> posts = query.getResultList();
        GetPost result = new GetPost();
        posts.forEach(post->{
            PostDto postDto = new PostDto();
            postDto.setText(post.getText());
            postDto.setTitle(post.getTitle());
            result.getPosts().add(postDto);
        });

        return result;
    }

    private Post createdPostRequestToPostEntity(CreatePostRequest createPostRequest){
        //TODO switch to auto mapping
        Post post = new Post();
        post.setText(createPostRequest.getText());
        post.setTitle(createPostRequest.getTitle());
        //TODO check whether user is exists
        post.setId(createPostRequest.getUserId());

        Set<Tag> tags = new HashSet<>();
        createPostRequest.getTags().forEach(tagDto->{
            Tag tag = new Tag();
            tag.setName(tagDto.getName());
            tags.add(tag);
        });
        post.setTags(tags);

        return post;
    }
}
