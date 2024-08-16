package com.blog.application.blog.helpers.params;


import com.blog.application.blog.helpers.params.utils.ExtendedStringUtils;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class GetPostsQueryBuilder {
    public String buildQuery(PostSearchParams postSearchParams) {
        //TODO extract parameters to static variables
        StringBuilder queryBuilder = new StringBuilder();
        if (postSearchParams == null) {
            //TODO will be changed.
            queryBuilder.append("SELECT p from Post p");
        } else {
            queryBuilder = new StringBuilder("SELECT p FROM Post p WHERE 1=1");
            Map<String, Object> parameters = new HashMap<>();
            if (!ExtendedStringUtils.isNull(postSearchParams.getText())) {
                queryBuilder.append(" AND p.text =:text");
            }
            if (!ExtendedStringUtils.isNull(postSearchParams.getTitle())) {
                queryBuilder.append(" AND p.title =:title");
            }
            if (postSearchParams.getUserId() != null) {
                queryBuilder.append(" AND p.user.id =:userId");
            }
        }

        return queryBuilder.toString();
    }

    public Map<String, Object> getParameters(PostSearchParams postSearchParams) {
        Map<String, Object> parameters = new HashMap<>();
        if (postSearchParams != null) {
            if (!ExtendedStringUtils.isNull(postSearchParams.getText())) {
                parameters.put("text", postSearchParams.getText());
            }
            if (!ExtendedStringUtils.isNull(postSearchParams.getTitle())) {
                parameters.put("title", postSearchParams.getTitle());
            }
            if (postSearchParams.getUserId() != null) {
                parameters.put("userId", postSearchParams.getUserId());
            }

        }
        return parameters;
    }
}
