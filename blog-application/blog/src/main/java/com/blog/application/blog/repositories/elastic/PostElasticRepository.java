package com.blog.application.blog.repositories.elastic;

import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import com.blog.application.blog.entities.elastic.ElasticPost;

import java.util.List;

public interface PostElasticRepository extends ElasticsearchRepository<ElasticPost, Long> {
    @Query("{" +
            "  \"bool\": {" +
            "    \"should\": [" +
            "      {\"match\": {\"title\": \"?0\"}}," +
            "      {\"match\": {\"text\": \"?0\"}}," +
            "      {\"nested\": {" +
            "        \"path\": \"tags\"," +
            "        \"query\": {" +
            "          \"match\": {\"tags.name\": \"?0\"}" +
            "        }" +
            "      }}" +
            "    ]" +
            "  }" +
            "}")
    List<ElasticPost> searchByKeyword(String keyword);
}
