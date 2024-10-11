package com.blog.application.blog.services.client;

import com.blog.application.blog.dtos.responses.client.UserClientDto;
import com.blog.application.blog.helpers.params.utils.UserFeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "user-service",configuration = UserFeignClientConfig.class)

public interface UserFeignClient {
    @GetMapping(value = "/api/users/getUserDetails", consumes = "application/json")
    public ResponseEntity<UserClientDto> getUserDetails();
}
