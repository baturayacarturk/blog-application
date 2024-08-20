package com.blog.application.blog.services.tag;

import com.blog.application.blog.dtos.common.TagDto;
import com.blog.application.blog.dtos.responses.post.AddTagResponse;
import com.blog.application.blog.entities.Post;
import com.blog.application.blog.entities.Tag;
import com.blog.application.blog.exceptions.messages.PostExceptionMessages;
import com.blog.application.blog.exceptions.messages.TagExceptionMessages;
import com.blog.application.blog.exceptions.types.BusinessException;
import com.blog.application.blog.helpers.params.TagSearchParams;
import com.blog.application.blog.helpers.params.utils.ExtendedStringUtils;
import com.blog.application.blog.repositories.TagRepository;
import com.blog.application.blog.services.post.PostService;

import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.blog.application.blog.helpers.params.TagQueryClauses.*;

@Service
public class TagServiceImpl implements TagService {
    private final TagRepository tagRepository;
    private final EntityManager entityManager;
    private final PostService postService;

    public TagServiceImpl(TagRepository tagRepository, EntityManager entityManager, PostService postService) {
        this.tagRepository = tagRepository;
        this.entityManager = entityManager;
        this.postService = postService;
    }

    @Override
    @Transactional
    public AddTagResponse addTagToPost(Long postId, TagDto tagDto) {
        Tag tag = new Tag();
        tag.setName(tagDto.getName());
        tagRepository.save(tag);
        postService.addTagToPost(postId, tag);
        AddTagResponse response = new AddTagResponse();
        response.setName(tagDto.getName());
        response.setPostId(postId);
        return response;
    }

    @Override
    public List<Tag> getTagEntities(Long tagId, String tagName) {
        TagSearchParams tagSearchParams = TagSearchParams.builder()
                .id(tagId)
                .name(tagName)
                .build();
        String queryString = buildTagBaseQuery();
        String finalQuery = buildWhereClause(queryString, tagSearchParams);
        Map<String, Object> parameters = getParameters(tagSearchParams);
        TypedQuery<Tag> query = entityManager.createQuery(finalQuery, Tag.class);
        parameters.forEach(query::setParameter);
        return query.getResultList();
    }

    @Override
    public TagDto removeTag(Long postId, Long tagId) {
        //TODO add validations
        List<Post> post = postService.getPostEntities(postId, null, null, null, null);
        if (ExtendedStringUtils.listIsNullOrEmpty(post))
            throw new BusinessException(String.format(PostExceptionMessages.POST_COULD_NOT_FOUND, postId));

        Optional<Tag> tagToRemove = post.get(0).getTags().stream().filter(tag -> tag.getId().equals(tagId)).findFirst();
        if (tagToRemove.isEmpty()) {
            throw new BusinessException(String.format(TagExceptionMessages.TAG_COULD_NOT_FOUND, tagId));
        }
        post.get(0).getTags().remove(tagToRemove.get());
        postService.removeTagFromPost(post.get(0));
        TagDto tagDto = new TagDto();
        tagDto.setName(tagToRemove.get().getName());
        return tagDto;
    }

    private String buildTagBaseQuery() {
        return SELECT_T_FROM_TAG;
    }

    private String buildWhereClause(String queryWithJoin, TagSearchParams tagSearchParams) {
        StringBuilder whereBuilder = new StringBuilder(queryWithJoin);
        whereBuilder.append(WHERE_CONDITION);

        if (tagSearchParams.getName() != null && !tagSearchParams.getName().isEmpty()) {
            whereBuilder
                    .append(AND_WITH_T_ALIAS)
                    .append(NAME)
                    .append(EQUALS_CONDITION)
                    .append(NAME);
        }
        if (tagSearchParams.getId() != null) {
            whereBuilder.append(AND_WITH_T_ALIAS)
                    .append(ID)
                    .append(EQUALS_CONDITION)
                    .append(ID);
        }
        return whereBuilder.toString();
    }


    private Map<String, Object> getParameters(TagSearchParams tagSearchParams) {
        Map<String, Object> parameters = new HashMap<>();
        if (tagSearchParams != null) {
            addParameterIfNotNull(parameters, ID, tagSearchParams.getId());
            addParameterIfNotNull(parameters, NAME, tagSearchParams.getName());
        }
        return parameters;
    }


    private void addParameterIfNotNull(Map<String, Object> parameters, String key, Object value) {
        if (value != null && !(value instanceof String && ((String) value).isEmpty())) {
            parameters.put(key, value);
        }
    }
}
