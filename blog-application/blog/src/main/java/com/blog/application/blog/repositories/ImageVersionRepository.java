package com.blog.application.blog.repositories;


import com.blog.application.blog.entities.ImageVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface ImageVersionRepository extends JpaRepository<ImageVersion, Long> {
    Optional<ImageVersion> findByImagePostIdAndIsOriginal(Long postId, boolean isOriginal);
    List<ImageVersion> findByImageId(Long imageId);
    @Query("SELECT vv FROM Image v JOIN v.versions vv WHERE v.id = :imageId AND vv.isOriginal = true")
    ImageVersion findOriginalVersionByVideoId(@Param("imageId") Long imageId);

}