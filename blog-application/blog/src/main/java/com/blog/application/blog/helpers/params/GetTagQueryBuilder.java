package com.blog.application.blog.helpers.params;

import com.blog.application.blog.helpers.params.utils.ExtendedStringUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class GetTagQueryBuilder {
    public String buildQuery(TagSearchParams tagSearchParams) {
        StringBuilder queryBuilder = new StringBuilder("SELECT t FROM Tag t");
        if (tagSearchParams.getId() != null) {
            queryBuilder.append(" AND p.id = :id");
        }
        if (!ExtendedStringUtils.isNull(tagSearchParams.getName())) {
            queryBuilder.append(" AND t.name = :name");
        }

        return queryBuilder.toString();
    }

    public Map<String, Object> getParameters(TagSearchParams tagSearchParams) {
        Map<String, Object> parameters = new HashMap<>();
        if (tagSearchParams != null) {
            if (tagSearchParams.getId() != null) {
                parameters.put("id", tagSearchParams.getId());
            }
            if (!ExtendedStringUtils.isNull(tagSearchParams.getName())) {
                parameters.put("name", tagSearchParams.getName());
            }
        }
        return parameters;
    }
}
