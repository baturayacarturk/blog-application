package com.blog.application.blog.services.tag;

import com.blog.application.blog.dtos.common.TagDto;
import com.blog.application.blog.dtos.responses.post.AddTagResponse;
import com.blog.application.blog.entities.Post;
import com.blog.application.blog.entities.Tag;
import com.blog.application.blog.helpers.params.GetTagQueryBuilder;
import com.blog.application.blog.helpers.params.TagSearchParams;
import com.blog.application.blog.repositories.TagRepository;
import com.blog.application.blog.services.post.PostService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TagServiceImpl implements TagService {
    private final TagRepository tagRepository;
    private final GetTagQueryBuilder getTagQueryBuilder;
    private final EntityManager entityManager;
    private final PostService postService;

    public TagServiceImpl(TagRepository tagRepository, GetTagQueryBuilder getTagQueryBuilder, EntityManager entityManager, PostService postService) {
        this.tagRepository = tagRepository;
        this.getTagQueryBuilder = getTagQueryBuilder;
        this.entityManager = entityManager;
        this.postService = postService;
    }

    @Override
    public AddTagResponse addTagToPost(Long postId, TagDto tagDto) {
        //TODO add validation whether given post might bu null (tagDto)->validate
        Tag tag = new Tag();
        tag.setName(tagDto.getName());
        tagRepository.save(tag);
        postService.addTagToPost(postId,tag);
        AddTagResponse response = new AddTagResponse();
        response.setName(tagDto.getName());
        response.setPostId(postId);
        return  response;
    }

    @Override
    public List<Tag> getTagEntities(Long tagId, String tagName) {
        TagSearchParams tagSearchParams = TagSearchParams.builder()
                .id(tagId)
                .name(tagName)
                .build();
        String queryString = getTagQueryBuilder.buildQuery(tagSearchParams);
        Map<String, Object> parameters = getTagQueryBuilder.getParameters(tagSearchParams);
        TypedQuery<Tag> query = entityManager.createQuery(queryString, Tag.class);
        parameters.forEach(query::setParameter);
        List<Tag> tags = query.getResultList();
        return tags;
    }

    @Override
    public TagDto removeTag(Long postId, Long tagId) {
        //TODO add validations
        List<Post> post = postService.getPostEntities(postId,null,null,null,null);
        Optional<Tag> tagToRemove = post.get(0).getTags().stream().filter(tag-> tag.getId().equals(tagId)).findFirst();
        if(tagToRemove.isEmpty()){
            //TODO Throw
        }
        post.get(0).getTags().remove(tagToRemove.get());
        postService.removeTagFromPost(post.get(0));
        TagDto tagDto = new TagDto();
        tagDto.setName(tagToRemove.get().getName());
        return tagDto;
    }
}
