package org.rocs.asa.service.category.impl;

import org.rocs.asa.dto.CategoryRequest;
import org.rocs.asa.repository.category.CategoryRepository;
import org.rocs.asa.service.category.CategoryService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }
    @Override
    public List<CategoryRequest> getAll() {
        return categoryRepository.findAll().stream()
                .map(c -> new CategoryRequest(c.getCategoryId(), c.getCategoryName()))
                .toList();
    }
}