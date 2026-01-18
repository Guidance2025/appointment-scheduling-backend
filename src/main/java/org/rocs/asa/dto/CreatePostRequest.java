package org.rocs.asa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreatePostRequest {
        @NotBlank(message = "Post content cannot be empty")
        @Size(max = 500, message = "Post content cannot exceed 500 characters")
        private String postContent;

        @NotBlank(message = "Category name is required")
        private String categoryName;

        // Single sectionId instead of sectionIds
        private Long sectionId;

        // Getters and setters
        public String getPostContent() { return postContent; }
        public void setPostContent(String postContent) { this.postContent = postContent; }
        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
        public Long getSectionId() { return sectionId; }
        public void setSectionId(Long sectionId) { this.sectionId = sectionId; }
}