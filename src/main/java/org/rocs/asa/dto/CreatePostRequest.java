package org.rocs.asa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreatePostRequest {
        @NotBlank(message = "Post content cannot be empty")
        @Size(max = 500, message = "Post content cannot exceed 500 characters")
        private String postContent;

        @NotBlank(message = "Category name is required")
        private String categoryName;

        private String sectionName;
        private Long sectionId;
        private List<String> sectionNames;
}