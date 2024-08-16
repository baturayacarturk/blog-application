package com.blog.application.blog.audit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

@Configuration
public class AuditorConfig {
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.of("staticUser"); //Some user when jwt added can be handled
    }
}
