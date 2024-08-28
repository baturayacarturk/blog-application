package com.blog.application.blog;

import com.blog.application.blog.entities.Post;
import com.blog.application.blog.entities.Tag;
import com.blog.application.blog.repositories.PostRepository;
import com.blog.application.blog.repositories.TagRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final PostRepository postRepository;
    private final TagRepository tagRepository;

    public DataInitializer(PostRepository postRepository, TagRepository tagRepository) {
        this.postRepository = postRepository;
        this.tagRepository = tagRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        postRepository.deleteAll();
        tagRepository.deleteAll();
        Tag techTag = new Tag();
        techTag.setName("Tech");
        tagRepository.save(techTag);

        Tag springTag = new Tag();
        springTag.setName("Spring");
        tagRepository.save(springTag);

        Tag javaTag = new Tag();
        javaTag.setName("Java");
        tagRepository.save(javaTag);

        Post post1 = new Post();
        post1.setTitle("Tech Trends 2024");
        post1.setText("An overview of the latest tech trends for 2024.");
        post1.getTags().add(techTag);
        post1.getTags().add(javaTag);
        postRepository.save(post1);

        Post post2 = new Post();
        post2.setTitle("Spring Boot Best Practices");
        post2.setText("A guide to best practices when working with Spring Boot.");
        post2.getTags().add(springTag);
        post2.getTags().add(techTag);
        postRepository.save(post2);

        Post post3 = new Post();
        post3.setTitle("Understanding Java Streams");
        post3.setText("Deep dive into Java Streams and their practical uses.");
        post3.getTags().add(javaTag);
        post3.getTags().add(springTag);
        postRepository.save(post3);
    }
}
