-- V1__Create_image_and_video_tables.sql
CREATE TABLE image (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    post_id BIGINT NOT NULL,
    storage_type VARCHAR(20) NOT NULL,
    created_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE
);

CREATE TABLE image_version (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    image_id BIGINT NOT NULL,
    version_name VARCHAR(50) NOT NULL,
    file_path VARCHAR(1000) NOT NULL,
    width INT NOT NULL,
    height INT NOT NULL,
    is_original BOOLEAN NOT NULL,
    created_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (image_id) REFERENCES image(id) ON DELETE CASCADE
);

CREATE TABLE video (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    post_id BIGINT NOT NULL,
    storage_type VARCHAR(20) NOT NULL,
    created_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE
);

CREATE TABLE video_version (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    video_id BIGINT NOT NULL,
    version_name VARCHAR(50) NOT NULL,
    file_path VARCHAR(1000) NOT NULL,
    quality VARCHAR(20) NOT NULL,
    is_original BOOLEAN NOT NULL,
    created_by VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (video_id) REFERENCES video(id) ON DELETE CASCADE
);

CREATE INDEX idx_image_post_id ON image(post_id);
CREATE INDEX idx_video_post_id ON video(post_id);
CREATE INDEX idx_image_version_is_original ON image_version(is_original);
CREATE INDEX idx_video_version_is_original ON video_version(is_original);