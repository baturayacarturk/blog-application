package com.blog.application.blog.helpers.params.utils;

import com.blog.application.blog.exceptions.details.BusinessProblemDetails;
import com.blog.application.blog.exceptions.types.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.RequestInterceptor;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class UserFeignClientConfig {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = LogManager.getLogger(UserFeignClientConfig.class);


    @Bean
    public RequestInterceptor bearerTokenInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (requestAttributes != null) {
                HttpServletRequest request = requestAttributes.getRequest();
                String authorizationHeader = request.getHeader("Authorization");
                requestTemplate.header("Authorization", authorizationHeader);
            }

        };
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new ErrorDecoder() {
            @Override
            public Exception decode(String methodKey, Response response) {
                String errorMessage = "An error occurred while calling the user service";
                try (InputStream bodyIs = response.body().asInputStream()) {
                    String bodyString = new String(bodyIs.readAllBytes(), StandardCharsets.UTF_8);
                    var problemDetails = objectMapper.readValue(bodyString, BusinessProblemDetails.class);
                    errorMessage = problemDetails.getDetail();
                } catch (IOException e) {
                    logger.error("Error reading response body", e);
                }
                return new BusinessException(errorMessage);
            }
        };
    }
}