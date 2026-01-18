package org.rocs.asa.dto;

public class CategoryRequest {
    private Long categoryId;
    private String categoryName;

    public CategoryRequest() {}
    public CategoryRequest(Long categoryId, String categoryName) {
        this.categoryId = categoryId; this.categoryName = categoryName;
    }
}