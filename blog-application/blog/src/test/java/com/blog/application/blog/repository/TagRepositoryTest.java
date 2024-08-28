package com.blog.application.blog.repository;

import com.blog.application.blog.entities.Tag;
import com.blog.application.blog.repositories.TagRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class TagRepositoryTest {

    @Autowired
    private TagRepository tagRepository;

    @Test
    void testCreateTag() {
        Tag tag = new Tag();
        tag.setName("New Tag");
        Tag savedTag = tagRepository.save(tag);

        assertThat(savedTag).isNotNull();
        assertThat(savedTag.getId()).isNotNull();
        assertThat(savedTag.getName()).isEqualTo("New Tag");
    }

    @Test
    void testFindTagById() {
        Tag tag = new Tag();
        tag.setName("Tag to Find");
        Tag savedTag = tagRepository.save(tag);

        Tag foundTag = tagRepository.findById(savedTag.getId()).orElse(null);

        assertThat(foundTag).isNotNull();
        assertThat(foundTag.getName()).isEqualTo("Tag to Find");
    }

    @Test
    void testUpdateTag() {
        Tag tag = new Tag();
        tag.setName("Old Name");
        Tag savedTag = tagRepository.save(tag);

        savedTag.setName("Updated Name");
        Tag updatedTag = tagRepository.save(savedTag);

        Tag foundTag = tagRepository.findById(updatedTag.getId()).orElse(null);

        assertThat(foundTag).isNotNull();
        assertThat(foundTag.getName()).isEqualTo("Updated Name");
    }

    @Test
    void testDeleteTag() {
        Tag tag = new Tag();
        tag.setName("Tag to Delete");
        Tag savedTag = tagRepository.save(tag);

        tagRepository.deleteById(savedTag.getId());

        Tag foundTag = tagRepository.findById(savedTag.getId()).orElse(null);
        assertThat(foundTag).isNull();
    }

    @Test
    void testFindAllTags() {
        Tag tag1 = new Tag();
        tag1.setName("Tag 1");
        tagRepository.save(tag1);

        Tag tag2 = new Tag();
        tag2.setName("Tag 2");
        tagRepository.save(tag2);

        Iterable<Tag> tags = tagRepository.findAll();

        assertThat(tags).isNotEmpty();
        assertThat(tags).contains(tag1, tag2);
    }
}
