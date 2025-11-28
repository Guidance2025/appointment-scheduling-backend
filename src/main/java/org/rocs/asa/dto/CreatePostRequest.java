package org.rocs.asa.dto;

import jakarta.validation.constraints.NotBlank;

public class CreatePostRequest {
        private Long sectionId;                          // optional
        private Long categoryId;                          // ignored for auto-insert flow (kept for future)
        @NotBlank(message = "Category name is required")
        private String categoryName;                      // typed text
        @NotBlank(message = "Post content cannot be empty")
        private String postContent;

        public Long getSectionId() { return sectionId; }
        public void setSectionId(Long sectionId) { this.sectionId = sectionId; }
        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
        public String getPostContent() { return postContent; }
        public void setPostContent(String postContent) { this.postContent = postContent; }
}
