/*
    Class Name: PhotoDto
    Description: Lightweight JSON representation of a photo (excludes binary data) for the REST API.
    Date Created: 2026-06-10
*/

package com.photoalbum.model;

import java.time.LocalDateTime;

/**
 * Data transfer object exposing photo metadata to the React front-end.
 * Deliberately omits the raw {@code photoData} BLOB so list/detail responses stay small.
 */
public class PhotoDto {

    private String id;
    private String originalFileName;
    private String filePath;
    private Long fileSize;
    private String mimeType;
    private LocalDateTime uploadedAt;
    private Integer width;
    private Integer height;
    private String description;
    private String previousPhotoId;
    private String nextPhotoId;

    public PhotoDto() {
    }

    /**
     * Build a DTO from a {@link Photo} entity without navigation links.
     */
    public static PhotoDto from(Photo photo) {
        PhotoDto dto = new PhotoDto();
        dto.id = photo.getId();
        dto.originalFileName = photo.getOriginalFileName();
        dto.filePath = photo.getFilePath();
        dto.fileSize = photo.getFileSize();
        dto.mimeType = photo.getMimeType();
        dto.uploadedAt = photo.getUploadedAt();
        dto.width = photo.getWidth();
        dto.height = photo.getHeight();
        dto.description = photo.getDescription();
        return dto;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPreviousPhotoId() {
        return previousPhotoId;
    }

    public void setPreviousPhotoId(String previousPhotoId) {
        this.previousPhotoId = previousPhotoId;
    }

    public String getNextPhotoId() {
        return nextPhotoId;
    }

    public void setNextPhotoId(String nextPhotoId) {
        this.nextPhotoId = nextPhotoId;
    }
}
