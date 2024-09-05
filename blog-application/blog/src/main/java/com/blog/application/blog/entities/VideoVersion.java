package com.blog.application.blog.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "video_version")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VideoVersion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "video_id", nullable = false)
    private Video video;

    @Column(nullable = false)
    private String versionName;

    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false)
    private String quality;

    @Column(nullable = false)
    private boolean isOriginal;
}