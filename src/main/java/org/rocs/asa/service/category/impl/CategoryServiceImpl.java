package org.rocs.asa.service.category.impl;

import org.rocs.asa.domain.category.Category;
import org.rocs.asa.dto.CategoryRequest;
import org.rocs.asa.repository.category.CategoryRepository;
import org.rocs.asa.service.category.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    private static final String EXIT_INTERVIEW = "EXIT INTERVIEW";

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<CategoryRequest> getAll() {
        return categoryRepository.findAllByOrderByCategoryNameAsc().stream()
                .map(c -> new CategoryRequest(c.getCategoryId(), c.getCategoryName()))
                .toList();
    }

    @Override
    @Transactional
    public Long getOrCreateExitInterviewCategoryId() {
        return getOrCreateByName(EXIT_INTERVIEW);
    }

    @Override
    @Transactional
    public Long getOrCreateByName(String categoryName) {
        return categoryRepository.findByCategoryNameIgnoreCase(categoryName)
                .map(Category::getCategoryId)
                .orElseGet(() -> {
                    Category c = new Category();
                    c.setCategoryName(categoryName);
                    return categoryRepository.save(c).getCategoryId();
                });
    }
}
