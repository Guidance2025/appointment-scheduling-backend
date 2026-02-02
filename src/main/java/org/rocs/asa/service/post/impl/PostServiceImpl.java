package org.rocs.asa.service.post.impl;

import jakarta.persistence.EntityNotFoundException;
import org.rocs.asa.domain.category.Category;
import org.rocs.asa.domain.guidance.staff.GuidanceStaff;
import org.rocs.asa.domain.post.Post;
import org.rocs.asa.domain.section.Section;
import org.rocs.asa.domain.student.Student;
import org.rocs.asa.domain.user.User;
import org.rocs.asa.dto.CreatePostRequest;
import org.rocs.asa.repository.category.CategoryRepository;
import org.rocs.asa.repository.post.PostRepository;
import org.rocs.asa.repository.section.SectionRepository;
import org.rocs.asa.repository.student.StudentRepository;
import org.rocs.asa.service.guidance.GuidanceService;
import org.rocs.asa.service.post.PostService;
import org.rocs.asa.repository.user.UserRepository;
import org.rocs.asa.service.notification.NotificationService;
import org.rocs.asa.service.student.StudentService;
import org.rocs.asa.utils.security.enumeration.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostServiceImpl.class);

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final GuidanceService guidanceService;
    private final JdbcTemplate jdbcTemplate;
    private final StudentRepository studentRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final SectionRepository sectionRepository;
    private final StudentService studentService;

    @Autowired
    public PostServiceImpl(PostRepository postRepository, CategoryRepository categoryRepository, GuidanceService guidanceService, JdbcTemplate jdbcTemplate, StudentRepository studentRepository, NotificationService notificationService, UserRepository userRepository, SectionRepository sectionRepository, StudentService studentService, NotificationService notificationService1) {
        this.postRepository = postRepository;
        this.categoryRepository = categoryRepository;
        this.guidanceService = guidanceService;
        this.jdbcTemplate = jdbcTemplate;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.sectionRepository = sectionRepository;
        this.studentService = studentService;
        this.notificationService = notificationService1;
    }

    @Override
    @Transactional
    public Post createPost(CreatePostRequest request) {
        String content = (request.getPostContent() == null) ? "" : request.getPostContent().trim();
        if (content.isEmpty()) throw new IllegalArgumentException("Post content cannot be empty");

        GuidanceStaff staff = guidanceService.findAuthenticatedGuidanceStaff();
        Long employeeNumber = staff.getEmployeeNumber();

        String rawName = (request.getCategoryName() == null) ? "" : request.getCategoryName().trim();
        if (rawName.isEmpty()) throw new IllegalArgumentException("Category name is required");

        String normalized = rawName.replaceAll("\\s+", " ").trim();
        if (normalized.equalsIgnoreCase("quote") || normalized.equalsIgnoreCase("qoute")) {
            normalized = "Quote";
        } else if (normalized.equalsIgnoreCase("announcement")) {
            normalized = "Announcement";
        } else if (normalized.equalsIgnoreCase("events") || normalized.equalsIgnoreCase("event")) {
            normalized = "Events";
        } else {
            normalized = normalized.substring(0, 1).toUpperCase() + normalized.substring(1);
        }
        String capped64 = normalized.length() > 64 ? normalized.substring(0, 64) : normalized;

        if (content.length() > 500) content = content.substring(0, 500);

        Long sectionId = resolveSectionId(request);

        // FIXED: Changed SYSTIMESTAMP to CURRENT_TIMESTAMP, used proper PostgreSQL INTERVAL syntax
        // FIXED: Cast parameter to help PostgreSQL determine type when sectionId is NULL
        Integer exists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tbl_posts p JOIN tbl_category c ON p.category_id = c.category_id " +
                        "WHERE p.employee_number = ? AND UPPER(TRIM(c.category_name)) = UPPER(TRIM(?)) " +
                        "AND (p.section_id = ? OR (p.section_id IS NULL AND CAST(? AS BIGINT) IS NULL)) " +
                        "AND p.posted_date >= CURRENT_TIMESTAMP - INTERVAL '5 second'",
                Integer.class, employeeNumber, capped64, sectionId, sectionId
        );

        if (exists != null && exists > 0) {
            LOGGER.warn("Duplicate create ignored (emp={}, catName='{}', section={})", employeeNumber, capped64, sectionId);
            return null;
        }

        Optional<Category> existingCategory = categoryRepository.findByCategoryNameIgnoreCase(capped64);
        Category savedCategory;
        if (existingCategory.isPresent()) {
            savedCategory = existingCategory.get();
            LOGGER.debug("Reusing existing category: {}", capped64);
        } else {
            Category toSave = new Category();
            toSave.setCategoryName(capped64);
            savedCategory = categoryRepository.save(toSave);
            LOGGER.debug("Created new category: {}", capped64);
        }

        Post post = new Post();
        post.setEmployeeNumber(employeeNumber);
        post.setCategoryId(savedCategory.getCategoryId());
        post.setPostContent(content);
        post.setPostedDate(LocalDateTime.now());
        post.setSectionId(sectionId);

        Post saved = postRepository.save(post);
        LOGGER.info("Post created id={} by employeeNumber={} with category '{}' and section={}",
                saved.getPostId(), employeeNumber, savedCategory.getCategoryName(), sectionId);

        try {
            List<String> studentUserIds = userRepository.findAllByRole(Role.STUDENT_ROLE.name())
                    .stream()
                    .map(User::getUserId)
                    .collect(Collectors.toList());
            notificationService.sendNotificationToAllStudent(
                    studentUserIds,
                    "New Post from Guidance",
                    "A new " + capped64.toLowerCase() + " has been posted. Check your Content Hub!",
                    "POST_UPDATE"
            );
        } catch (Exception e) {
            LOGGER.error("Failed to send notification for post creation: {}", e.getMessage());
        }

        return saved;
    }

    @Override
    @Transactional
    public Post createQuoteOfTheDay(CreatePostRequest request) {
        CreatePostRequest quoteRequest = new CreatePostRequest();
        quoteRequest.setPostContent(request.getPostContent());
        quoteRequest.setCategoryName("Quote");

        return createPost(quoteRequest);
    }

    @Override
    public List<Map<String, Object>> getAllPosts(int limit) {
        String sql =
                "SELECT * FROM ( " +
                        "  SELECT t.* FROM ( " +
                        "    SELECT " +
                        "      p.post_id, " +
                        "      p.post_content, " +
                        "      p.posted_date, " +
                        "      c.category_name, " +
                        "      s.section_name, " +
                        "      s.organization, " +
                        "      TRIM(COALESCE(per.first_name, '') || ' ' || COALESCE(per.last_name, '')) AS posted_by, " +
                        "      ROW_NUMBER() OVER (PARTITION BY p.post_id ORDER BY p.posted_date DESC) AS rn " +
                        "    FROM tbl_posts p " +
                        "    JOIN tbl_category c ON p.category_id = c.category_id " +
                        "    LEFT JOIN tbl_section s ON p.section_id = s.section_id " +
                        "    LEFT JOIN tbl_guidance_staff gs ON p.employee_number = gs.employee_number " +
                        "    LEFT JOIN tbl_person per ON gs.person_id = per.id " +
                        "    WHERE UPPER(TRIM(c.category_name)) <> 'QUOTE' " +
                        "  ) t " +
                        "  WHERE t.rn = 1 " +
                        "  ORDER BY t.posted_date DESC " +
                        ") AS subquery LIMIT ?";

        return jdbcTemplate.queryForList(sql, limit);
    }

    @Override
    public Map<String, Object> getQuoteOfTheDay() {
        String todaySql =
                "SELECT p.post_id, p.post_content, p.posted_date, " +
                        "       s.section_name, s.organization " +
                        "FROM tbl_posts p " +
                        "JOIN tbl_category c ON p.category_id = c.category_id " +
                        "LEFT JOIN tbl_section s ON p.section_id = s.section_id " +
                        "WHERE UPPER(TRIM(c.category_name)) = 'QUOTE' " +
                        "  AND DATE(p.posted_date) = CURRENT_DATE " +
                        "ORDER BY p.posted_date DESC " +
                        "LIMIT 1";
        try {
            return jdbcTemplate.queryForMap(todaySql);
        } catch (EmptyResultDataAccessException e) {
            String latestSql =
                    "SELECT p.post_id, p.post_content, p.posted_date, " +
                            "       s.section_name, s.organization " +
                            "FROM tbl_posts p " +
                            "JOIN tbl_category c ON p.category_id = c.category_id " +
                            "LEFT JOIN tbl_section s ON p.section_id = s.section_id " +
                            "WHERE UPPER(TRIM(c.category_name)) = 'QUOTE' " +
                            "ORDER BY p.posted_date DESC " +
                            "LIMIT 1";
            try {
                return jdbcTemplate.queryForMap(latestSql);
            } catch (EmptyResultDataAccessException ex) {
                return new HashMap<>();
            }
        }
    }

    @Override
    public Map<String, Object> getFeed(int limit) {
        String postsSql =
                "SELECT * FROM ( " +
                        "  SELECT t.* FROM ( " +
                        "    SELECT " +
                        "      p.post_id, " +
                        "      p.post_content, " +
                        "      p.posted_date, " +
                        "      c.category_name, " +
                        "      s.section_name, " +
                        "      s.organization, " +
                        "      TRIM(COALESCE(per.first_name, '') || ' ' || COALESCE(per.last_name, '')) AS posted_by, " +
                        "      ROW_NUMBER() OVER (PARTITION BY p.post_id ORDER BY p.posted_date DESC) AS rn " +
                        "    FROM tbl_posts p " +
                        "    JOIN tbl_category c ON p.category_id = c.category_id " +
                        "    LEFT JOIN tbl_section s ON p.section_id = s.section_id " +
                        "    LEFT JOIN tbl_guidance_staff gs ON p.employee_number = gs.employee_number " +
                        "    LEFT JOIN tbl_person per ON gs.person_id = per.id " +
                        "    WHERE UPPER(TRIM(c.category_name)) <> 'QUOTE' " +
                        "  ) t " +
                        "  WHERE t.rn = 1 " +
                        "  ORDER BY t.posted_date DESC " +
                        ") AS subquery LIMIT ?";

        String quoteTodaySql =
                "SELECT p.post_id, p.post_content, p.posted_date, s.section_name, s.organization " +
                        "FROM tbl_posts p " +
                        "JOIN tbl_category c ON p.category_id = c.category_id " +
                        "LEFT JOIN tbl_section s ON p.section_id = s.section_id " +
                        "WHERE UPPER(TRIM(c.category_name)) = 'QUOTE' " +
                        "  AND DATE(p.posted_date) = CURRENT_DATE " +
                        "ORDER BY p.posted_date DESC " +
                        "LIMIT 1";

        String quoteLatestSql =
                "SELECT p.post_id, p.post_content, p.posted_date, s.section_name, s.organization " +
                        "FROM tbl_posts p " +
                        "JOIN tbl_category c ON p.category_id = c.category_id " +
                        "LEFT JOIN tbl_section s ON p.section_id = s.section_id " +
                        "WHERE UPPER(TRIM(c.category_name)) = 'QUOTE' " +
                        "ORDER BY p.posted_date DESC " +
                        "LIMIT 1";

        Map<String, Object> payload = new HashMap<>();
        List<Map<String, Object>> posts = jdbcTemplate.queryForList(postsSql, limit);
        payload.put("posts", posts);

        try {
            Map<String, Object> quote = jdbcTemplate.queryForMap(quoteTodaySql);
            payload.put("quote", quote);
        } catch (EmptyResultDataAccessException e) {
            try {
                Map<String, Object> quote = jdbcTemplate.queryForMap(quoteLatestSql);
                payload.put("quote", quote);
            } catch (EmptyResultDataAccessException ex) {
                payload.put("quote", new HashMap<>());
            }
        }

        return payload;
    }

    @Override
    @Transactional
    public void deletePost(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new EntityNotFoundException("Post not found: " + postId);
        }
        postRepository.deleteById(postId);
        LOGGER.info("Post deleted id={}", postId);
    }

    @Override
    public List<Map<String, Object>> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(category -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("categoryId", category.getCategoryId());
                    map.put("categoryName", category.getCategoryName());
                    return map;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getPostsForStudent(Long studentId, int limit) {
        Long studentSectionId = getStudentSectionId(studentId);

        // FIXED: Replaced ROWNUM with LIMIT, changed NVL to COALESCE
        String sql = "SELECT * FROM ( " +
                "  SELECT p.post_id, p.post_content, p.posted_date, c.category_name, " +
                "         s.section_name, s.organization, " +
                "         TRIM(COALESCE(per.first_name, '') || ' ' || COALESCE(per.last_name, '')) AS posted_by " +
                "  FROM tbl_posts p " +
                "  JOIN tbl_category c ON p.category_id = c.category_id " +
                "  LEFT JOIN tbl_section s ON p.section_id = s.section_id " +
                "  LEFT JOIN tbl_guidance_staff gs ON p.employee_number = gs.employee_number " +
                "  LEFT JOIN tbl_person per ON gs.person_id = per.id " +
                "  WHERE UPPER(TRIM(c.category_name)) <> 'QUOTE' " +
                "    AND (p.section_id IS NULL OR p.section_id = ?) " +
                "  ORDER BY p.posted_date DESC " +
                ") AS subquery LIMIT ?";
        return jdbcTemplate.queryForList(sql, studentSectionId, limit);
    }

    @Override
    public List<String> findStudentsSection() {
        return sectionRepository.findAllDistinctSectionName();
    }


    private Long getStudentSectionId(Long studentId) {
        Student student = studentRepository.findById(studentId).orElse(null);
        return student != null && student.getSection() != null ? student.getSection().getId() : null;
    }

    private Long resolveSectionId(CreatePostRequest request) {
        if (request.getSectionId() != null) {
            LOGGER.debug("Using provided section ID: {}", request.getSectionId());
            return request.getSectionId();
        }

        if (request.getSectionName() != null && !request.getSectionName().trim().isEmpty()) {
            String sectionName = request.getSectionName().trim();
            LOGGER.debug("Looking up section by name: {}", sectionName);

            Optional<Section> section = sectionRepository.findBySectionName(sectionName);

            if (section.isPresent()) {
                Long id = section.get().getId();
                LOGGER.info("Resolved section name '{}' to ID {}", sectionName, id);
                return id;
            } else {
                LOGGER.error("Section not found with name: {}", sectionName);
                throw new IllegalArgumentException("Invalid section: " + sectionName);
            }
        }

        LOGGER.debug("No section ID or name provided");
        return null;
    }
}