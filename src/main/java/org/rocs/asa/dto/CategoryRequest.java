package org.rocs.asa.dto;

public class CategoryRequest {
    private Long categoryId;
    private String categoryName;

    public CategoryRequest() {}
    public CategoryRequest(Long categoryId, String categoryName) {
        this.categoryId = categoryId; this.categoryName = categoryName;
    }
    public Long getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
}