package org.rocs.asa.service.question.impl;

import org.rocs.asa.domain.category.Category;
import org.rocs.asa.domain.question.Question;
import org.rocs.asa.dto.question.CreateOrUpdateQuestionRequest;
import org.rocs.asa.dto.question.QuestionDto;
import org.rocs.asa.repository.category.CategoryRepository;
import org.rocs.asa.repository.question.QuestionRepository;
import org.rocs.asa.service.question.QuestionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
public class QuestionServiceImpl implements QuestionService {

    private final QuestionRepository questionRepository;
    private final CategoryRepository categoryRepository;

    public QuestionServiceImpl(QuestionRepository questionRepository,
                               CategoryRepository categoryRepository) {
        this.questionRepository = questionRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<QuestionDto> list() {
        return questionRepository.findAll().stream()
                .sorted(Comparator
                        .comparing(Question::getDateCreated, Comparator.nullsLast(Comparator.naturalOrder())).reversed()
                        .thenComparing(Question::getId, Comparator.nullsLast(Comparator.reverseOrder()))
                )
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public QuestionDto create(CreateOrUpdateQuestionRequest req) {
        Question q = new Question();
        q.setQuestionText(req.text());
        q.setEmployeeNumber(req.employeeNumber()); 
        q.setDateCreated(LocalDate.now());
        q.setCategory(resolveCategory(req.category()));
        return toDto(questionRepository.save(q));
    }

    @Override
    @Transactional
    public QuestionDto update(Long id, CreateOrUpdateQuestionRequest req) {
        Question q = questionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));
        q.setQuestionText(req.text());
        q.setEmployeeNumber(req.employeeNumber());
        q.setCategory(resolveCategory(req.category()));
        return toDto(questionRepository.save(q));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        questionRepository.deleteById(id);
    }

    private Category resolveCategory(String name) {
        if (name == null || name.isBlank()) return null;
        return categoryRepository.findByCategoryNameIgnoreCase(name.trim())
                .orElseGet(() -> {
                    Category c = new Category();
                    c.setCategoryName(name.trim());
                    return categoryRepository.save(c);
                });
    }

    private QuestionDto toDto(Question q) {
        return new QuestionDto(
                q.getId(),
                q.getQuestionText(),
                q.getCategory() != null ? q.getCategory().getCategoryName() : null
        );
    }
}
