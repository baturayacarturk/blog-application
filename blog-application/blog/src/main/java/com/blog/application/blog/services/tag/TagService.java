package com.blog.application.blog.services.tag;

import com.blog.application.blog.dtos.common.TagDto;
import com.blog.application.blog.dtos.responses.post.AddTagResponse;
import com.blog.application.blog.entities.Tag;

import java.util.List;

public interface TagService {

    AddTagResponse addTagToPost(Long postId, TagDto tagDto);

    TagDto removeTag(Long postId, Long tagId);
}
