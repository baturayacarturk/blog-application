package com.blog.application.blog.dtos.common;

import java.util.List;

public record ElasticPostEvent(
        Long eventId,
        String eventType,
        String title,
        String text,
        Long userId,
        List<ElasticTagDto> tags

) {
    public ElasticPostEvent(Long eventId, String title, String text, Long userId, List<ElasticTagDto> tags) {
        this(eventId, "NEW", title, text, userId, tags);
    }
}
