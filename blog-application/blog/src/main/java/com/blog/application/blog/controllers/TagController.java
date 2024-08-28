package com.blog.application.blog.controllers;

import com.blog.application.blog.dtos.common.TagDto;
import com.blog.application.blog.dtos.responses.post.AddTagResponse;
import com.blog.application.blog.services.tag.TagService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/tags", produces = {MediaType.APPLICATION_JSON_VALUE})
@AllArgsConstructor
@Api(value = "Tag Management", tags = "Tags")
public class TagController {

    private static final Logger logger = LogManager.getLogger(TagController.class);

    private final TagService tagService;

    /**
     * Adds a tag to the specified post.
     *
     * @param postId the ID of the post to which the tag will be added
     * @param tagDto the details of the tag to be added
     * @return a {@link ResponseEntity} containing the details of the added tag
     */
    @ApiOperation(value = "Add a tag to a post",
            notes = "Adds a tag to the specified post by postId and returns a response with tag details.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Tag added to post successfully", response = AddTagResponse.class),
            @ApiResponse(code = 400, message = "Invalid input")
    })
    @PostMapping("/{postId}")
    public ResponseEntity<AddTagResponse> createPost(
            @ApiParam(value = "ID of the post to which the tag will be added", required = true)
            @PathVariable Long postId,
            @ApiParam(value = "Details of the tag to be added", required = true)
            @RequestBody TagDto tagDto) {

        logger.info("Received request to add tag to post with ID: {}, Tag details: {}", postId, tagDto);

        AddTagResponse createdTag = tagService.addTagToPost(postId, tagDto);

        logger.info("Response for adding tag to post with ID: {}: {}", postId, createdTag);

        return new ResponseEntity<>(createdTag, HttpStatus.CREATED);
    }

    /**
     * Removes a tag from the specified post.
     *
     * @param postId the ID of the post from which the tag will be removed
     * @param tagId the ID of the tag to be removed
     * @return a {@link ResponseEntity} containing the details of the removed tag
     */
    @ApiOperation(value = "Remove a tag from a post",
            notes = "Removes a tag from the specified post by postId and tagId and returns the removed tag details.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Tag removed from post successfully", response = TagDto.class),
            @ApiResponse(code = 400, message = "Invalid input"),
            @ApiResponse(code = 404, message = "Tag or post not found")
    })
    @DeleteMapping("/{postId}")
    public ResponseEntity<TagDto> removeTagFromPost(
            @ApiParam(value = "ID of the post from which the tag will be removed", required = true)
            @PathVariable Long postId,
            @ApiParam(value = "ID of the tag to be removed", required = true)
            @RequestParam Long tagId) {

        logger.info("Received request to remove tag with ID: {} from post with ID: {}", tagId, postId);

        TagDto removedTag = tagService.removeTag(postId, tagId);

        logger.info("Response for removing tag with ID: {} from post with ID: {}: {}", tagId, postId, removedTag);

        return new ResponseEntity<>(removedTag, HttpStatus.OK);
    }
}
