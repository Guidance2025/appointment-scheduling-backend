package org.rocs.asa.service.category;

import org.rocs.asa.dto.CategoryRequest;

import java.util.List;

public interface CategoryService {
    List<CategoryRequest> getAll();

    Long getOrCreateExitInterviewCategoryId();

    Long getOrCreateByName(String categoryName);
}
