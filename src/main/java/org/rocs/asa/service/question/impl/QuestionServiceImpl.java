package org.rocs.asa.service.question.impl;

import org.rocs.asa.domain.category.Category;
import org.rocs.asa.domain.guidance.staff.GuidanceStaff;
import org.rocs.asa.domain.question.Question;
import org.rocs.asa.exception.domain.EmptyFieldException;
import org.rocs.asa.exception.domain.QuestionNotFoundException;
import org.rocs.asa.repository.category.CategoryRepository;
import org.rocs.asa.repository.guidance.staff.GuidanceStaffRepository;
import org.rocs.asa.repository.question.QuestionRepository;
import org.rocs.asa.service.question.QuestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class QuestionServiceImpl implements QuestionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(QuestionServiceImpl.class);
    private static final Long SELF_ASSESSMENT_CATEGORY_ID = 4L;

    private final QuestionRepository questionRepository;
    private final GuidanceStaffRepository guidanceStaffRepository;
    private final CategoryRepository categoryRepository;

    @Autowired
    public QuestionServiceImpl(QuestionRepository questionRepository,
                               GuidanceStaffRepository guidanceStaffRepository,
                               CategoryRepository categoryRepository) {
        this.questionRepository = questionRepository;
        this.guidanceStaffRepository = guidanceStaffRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Question createQuestion(Question question, Long employeeNumber) {
        validateQuestionText(question.getQuestionText());
        GuidanceStaff staff = getGuidanceStaff(employeeNumber);
        Category category = categoryRepository.findById(SELF_ASSESSMENT_CATEGORY_ID)
                .orElseThrow(() -> new RuntimeException("Self-Assessment category not found"));

        question.setGuidanceStaff(staff);
        question.setCategory(category);
        question.setDateCreated(LocalDate.now());

        Question savedQuestion = questionRepository.save(question);
        LOGGER.info("Question created with ID: {}", savedQuestion.getQuestionId());
        return savedQuestion;
    }

    @Override
    public Question updateQuestion(Long questionId, Question updatedQuestion, Long employeeNumber) {
        validateQuestionText(updatedQuestion.getQuestionText());
        Question existingQuestion = getQuestionByIdAndEmployee(questionId, employeeNumber);

        existingQuestion.setQuestionText(updatedQuestion.getQuestionText());
        Question savedQuestion = questionRepository.save(existingQuestion);
        LOGGER.info("Question updated with ID: {}", questionId);
        return savedQuestion;
    }

    @Override
    public void deleteQuestion(Long questionId, Long employeeNumber) {
        Question question = getQuestionByIdAndEmployee(questionId, employeeNumber);
        questionRepository.delete(question);
        LOGGER.info("Question deleted with ID: {}", questionId);
    }

    @Override
    public List<Question> getQuestionsByEmployee(Long employeeNumber) {
        return questionRepository.findByCategory_CategoryIdAndGuidanceStaff_EmployeeNumber(SELF_ASSESSMENT_CATEGORY_ID, employeeNumber);
    }

    @Override
    public Question getQuestionByIdAndEmployee(Long questionId, Long employeeNumber) {
        return questionRepository.findById(questionId)
                .filter(q -> q.getGuidanceStaff().getEmployeeNumber().equals(employeeNumber) &&
                        q.getCategory().getCategoryId().equals(SELF_ASSESSMENT_CATEGORY_ID))
                .orElseThrow(() -> new QuestionNotFoundException("Question not found or access denied"));
    }

    private void validateQuestionText(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new EmptyFieldException("Question text cannot be empty");
        }
    }

    private GuidanceStaff getGuidanceStaff(Long employeeNumber) {
        return guidanceStaffRepository.findById(employeeNumber)
                .orElseThrow(() -> new RuntimeException("Guidance staff not found"));
    }
}