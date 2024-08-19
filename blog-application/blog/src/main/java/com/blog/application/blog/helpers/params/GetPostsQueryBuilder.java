package com.blog.application.blog.helpers.params;


import com.blog.application.blog.helpers.params.utils.ExtendedStringUtils;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class GetPostsQueryBuilder {
    public String buildQuery(PostSearchParams postSearchParams) {
        StringBuilder queryBuilder = new StringBuilder("SELECT p FROM Post p WHERE p.id IN (");
        queryBuilder.append("SELECT DISTINCT p.id FROM Post p LEFT JOIN p.tags t WHERE 1=1");

        if (!ExtendedStringUtils.isNull(postSearchParams.getTagNames())) {
            queryBuilder.append(" AND t.name = :tagNames");
        }
        queryBuilder.append(")");

        if (postSearchParams.getId() != null) {
            queryBuilder.append(" AND p.id = :id");
        }
        if (!ExtendedStringUtils.isNull(postSearchParams.getText())) {
            queryBuilder.append(" AND p.text = :text");
        }
        if (!ExtendedStringUtils.isNull(postSearchParams.getTitle())) {
            queryBuilder.append(" AND p.title = :title");
        }
        if (postSearchParams.getUserId() != null) {
            queryBuilder.append(" AND p.user.id = :userId");
        }

        return queryBuilder.toString();
    }

    public Map<String, Object> getParameters(PostSearchParams postSearchParams) {
        Map<String, Object> parameters = new HashMap<>();
        if (postSearchParams != null) {
            if (postSearchParams.getId() != null) {
                parameters.put("id", postSearchParams.getId());
            }
            if (!ExtendedStringUtils.isNull(postSearchParams.getText())) {
                parameters.put("text", postSearchParams.getText());
            }
            if (!ExtendedStringUtils.isNull(postSearchParams.getTitle())) {
                parameters.put("title", postSearchParams.getTitle());
            }
            if (postSearchParams.getUserId() != null) {
                parameters.put("userId", postSearchParams.getUserId());
            }
            if (!ExtendedStringUtils.isNull(postSearchParams.getTagNames())) {
                parameters.put("tagNames", postSearchParams.getTagNames());
            }

        }
        return parameters;
    }
}
