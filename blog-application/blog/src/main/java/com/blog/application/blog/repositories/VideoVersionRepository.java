package com.blog.application.blog.repositories;



import com.blog.application.blog.entities.VideoVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface VideoVersionRepository extends JpaRepository<VideoVersion, Long> {
    Optional<VideoVersion> findById(Long id);
    Optional<VideoVersion> findByVideoPostIdAndIsOriginal(Long postId, boolean isOriginal);
    @Query("SELECT vv FROM Video v JOIN v.versions vv WHERE v.id = :videoId AND vv.isOriginal = true")
    VideoVersion findOriginalVersionByVideoId(@Param("videoId") Long videoId);
    List<VideoVersion> findByVideoId(Long videoId);
}