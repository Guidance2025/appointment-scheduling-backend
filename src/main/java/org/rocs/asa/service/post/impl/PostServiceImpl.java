package org.rocs.asa.service.post.impl;

import org.rocs.asa.domain.category.Category;
import org.rocs.asa.domain.guidance.staff.GuidanceStaff;
import org.rocs.asa.domain.post.Post;
import org.rocs.asa.domain.question.Question;
import org.rocs.asa.domain.section.Section;
import org.rocs.asa.exception.domain.EmptyFieldException;
import org.rocs.asa.exception.domain.PostNotFoundException;
import org.rocs.asa.repository.category.CategoryRepository;
import org.rocs.asa.repository.guidance.staff.GuidanceStaffRepository;
import org.rocs.asa.repository.post.PostRepository;
import org.rocs.asa.repository.question.QuestionRepository;
import org.rocs.asa.repository.section.SectionRepository;
import org.rocs.asa.service.post.PostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PostServiceImpl implements PostService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PostServiceImpl.class);

    private final PostRepository postRepository;
    private final GuidanceStaffRepository guidanceStaffRepository;
    private final SectionRepository sectionRepository;
    private final CategoryRepository categoryRepository;
    private final QuestionRepository questionRepository;

    @Autowired
    public PostServiceImpl(PostRepository postRepository, GuidanceStaffRepository guidanceStaffRepository,
                           SectionRepository sectionRepository, CategoryRepository categoryRepository,
                           QuestionRepository questionRepository) {
        this.postRepository = postRepository;
        this.guidanceStaffRepository = guidanceStaffRepository;
        this.sectionRepository = sectionRepository;
        this.categoryRepository = categoryRepository;
        this.questionRepository = questionRepository;
    }

    @Override
    public Post createPost(Post post, Long employeeNumber) {
        validatePostContent(post.getPostContent());
        GuidanceStaff staff = getGuidanceStaff(employeeNumber);
        Section section = getSection(post.getSection().getSectionId());
        Category category = getCategory(post.getCategory().getCategoryId());
        Question question = post.getQuestion() != null ? getQuestion(post.getQuestion().getQuestionId()) : null;

        post.setGuidanceStaff(staff);
        post.setSection(section);
        post.setCategory(category);
        post.setQuestion(question);
        post.setPostedDate(LocalDateTime.now());

        Post savedPost = postRepository.save(post);
        LOGGER.info("Post created with ID: {}", savedPost.getPostId());
        return savedPost;
    }

    @Override
    public Post updatePost(Long postId, Post updatedPost, Long employeeNumber) {
        validatePostContent(updatedPost.getPostContent());
        Post existingPost = getPostByIdAndEmployee(postId, employeeNumber);

        existingPost.setPostContent(updatedPost.getPostContent());
        if (updatedPost.getSection() != null) existingPost.setSection(getSection(updatedPost.getSection().getSectionId()));
        if (updatedPost.getCategory() != null) existingPost.setCategory(getCategory(updatedPost.getCategory().getCategoryId()));
        if (updatedPost.getQuestion() != null) existingPost.setQuestion(getQuestion(updatedPost.getQuestion().getQuestionId()));

        Post savedPost = postRepository.save(existingPost);
        LOGGER.info("Post updated with ID: {}", postId);
        return savedPost;
    }

    @Override
    public void deletePost(Long postId, Long employeeNumber) {
        Post post = getPostByIdAndEmployee(postId, employeeNumber);
        postRepository.delete(post);
        LOGGER.info("Post deleted with ID: {}", postId);
    }

    @Override
    public List<Post> getPostsByStaff(Long employeeNumber) {
        return postRepository.findByGuidanceStaff_EmployeeNumber(employeeNumber);
    }

    @Override
    public List<Post> getPostsBySection(Long sectionId) {
        return postRepository.findBySection_SectionId(sectionId);
    }

    @Override
    public List<Post> getPostsByCategory(Long categoryId) {
        return postRepository.findByCategory_CategoryId(categoryId);
    }

    @Override
    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    @Override
    public Post getPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("Post not found"));
    }

    private Post getPostByIdAndEmployee(Long postId, Long employeeNumber) {
        Post post = getPostById(postId);
        if (!post.getGuidanceStaff().getEmployeeNumber().equals(employeeNumber)) {
            throw new PostNotFoundException("Post not found or access denied");
        }
        return post;
    }

    private void validatePostContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new EmptyFieldException("Post content cannot be empty");
        }
    }

    private GuidanceStaff getGuidanceStaff(Long employeeNumber) {
        return guidanceStaffRepository.findById(employeeNumber)
                .orElseThrow(() -> new RuntimeException("Guidance staff not found"));
    }

    private Section getSection(Long sectionId) {
        return sectionRepository.findById(sectionId)
                .orElseThrow(() -> new RuntimeException("Section not found"));
    }

    private Category getCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    private Question getQuestion(Long questionId) {
        return questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));
    }
}
