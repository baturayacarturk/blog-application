package com.blog.application.blog.config;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@Configuration
public class ElasticsearchIndexConfig {

    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("classpath:index-creation-script.json")
    private Resource indexConfigFile;

    public ElasticsearchIndexConfig(RestClient restClient) {
        this.restClient = restClient;
    }

    @PostConstruct
    public void createIndicesIfNotExist() throws IOException {
        try (InputStream inputStream = indexConfigFile.getInputStream()) {
            Map<String, Object> indexConfigurations = objectMapper.readValue(inputStream, Map.class);

            @SuppressWarnings("unchecked")
            Map<String, Map<String, Object>> indices = (Map<String, Map<String, Object>>) indexConfigurations.get("indices");

            for (Map.Entry<String, Map<String, Object>> entry : indices.entrySet()) {
                String indexName = entry.getKey();
                Map<String, Object> indexSettings = entry.getValue();

                Request request = new Request("HEAD", "/" + indexName);
                Response response = restClient.performRequest(request);

                if (response.getStatusLine().getStatusCode() == 404) {
                    request = new Request("PUT", "/" + indexName);
                    request.setJsonEntity(objectMapper.writeValueAsString(indexSettings));
                    restClient.performRequest(request);
                    System.out.println("Index " + indexName + " created successfully.");
                } else {
                    System.out.println("Index " + indexName + " already exists.");
                }
            }
        }
    }
}