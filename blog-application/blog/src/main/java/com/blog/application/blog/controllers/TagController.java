package com.blog.application.blog.controllers;

import com.blog.application.blog.dtos.common.TagDto;
import com.blog.application.blog.dtos.responses.post.AddTagResponse;
import com.blog.application.blog.services.tag.TagService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/tags", produces = {MediaType.APPLICATION_JSON_VALUE})
@AllArgsConstructor
public class TagController {

    private TagService tagService;

    @PostMapping("/{postId}")
    public ResponseEntity<AddTagResponse> createPost(@PathVariable Long postId, @RequestBody TagDto tagDto) {
        AddTagResponse createdTag = tagService.addTagToPost(postId,tagDto);
        return new ResponseEntity<>(createdTag, HttpStatus.CREATED);
    }
    @DeleteMapping("/{postId}")
    public ResponseEntity<TagDto> removeTagFromPost(@PathVariable Long postId, @RequestParam Long tagId) {
        TagDto removedTag = tagService.removeTag(postId,tagId);
        return new ResponseEntity<>(removedTag, HttpStatus.OK);
    }

}
