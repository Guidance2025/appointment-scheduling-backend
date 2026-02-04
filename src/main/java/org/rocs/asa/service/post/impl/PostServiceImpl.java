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
    public PostServiceImpl(PostRepository postRepository,
                           CategoryRepository categoryRepository,
                           GuidanceService guidanceService,
                           JdbcTemplate jdbcTemplate,
                           StudentRepository studentRepository,
                           NotificationService notificationService,
                           UserRepository userRepository,
                           SectionRepository sectionRepository,
                           StudentService studentService) {
        this.postRepository = postRepository;
        this.categoryRepository = categoryRepository;
        this.guidanceService = guidanceService;
        this.jdbcTemplate = jdbcTemplate;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.sectionRepository = sectionRepository;
        this.studentService = studentService;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public Post createPost(CreatePostRequest request) {
        // ADD THIS LOGGING AT THE START
        LOGGER.info("📥 CREATE POST REQUEST:");
        LOGGER.info("  - Category: {}", request.getCategoryName());
        LOGGER.info("  - Content length: {}", request.getPostContent() != null ? request.getPostContent().length() : 0);
        LOGGER.info("  - sectionNames: {}", request.getSectionNames());
        LOGGER.info("  - sectionName (old): {}", request.getSectionName());
        LOGGER.info("  - sectionId (old): {}", request.getSectionId());

        String content = (request.getPostContent() == null) ? "" : request.getPostContent().trim();
        if (content.isEmpty()) throw new IllegalArgumentException("Post content cannot be empty");

        GuidanceStaff staff = guidanceService.findAuthenticatedGuidanceStaff();
        Long employeeNumber = staff.getEmployeeNumber();

        String rawName = (request.getCategoryName() == null) ? "" : request.getCategoryName().trim();
        if (rawName.isEmpty()) throw new IllegalArgumentException("Category name is required");

        String normalized = normalizeCategory(rawName);
        String capped64 = normalized.length() > 64 ? normalized.substring(0, 64) : normalized;

        if (content.length() > 500) content = content.substring(0, 500);

        // Get category
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

        List<String> targetSectionNames = getTargetSectionNames(request);

        Post post = new Post();
        post.setEmployeeNumber(employeeNumber);
        post.setCategoryId(savedCategory.getCategoryId());
        post.setPostContent(content);
        post.setPostedDate(LocalDateTime.now());

        Post saved = postRepository.save(post);

        if (targetSectionNames.isEmpty()) {
            LOGGER.warn("⚠️ POST {} - NO SECTIONS SPECIFIED - SENDING TO ALL STUDENTS", saved.getPostId());
        } else {
            LOGGER.info("✅ POST {} - Targeting {} section(s): {}",
                    saved.getPostId(), targetSectionNames.size(), targetSectionNames);
        }

        try {
            sendNotificationsForPost(saved, targetSectionNames, capped64);
        } catch (Exception e) {
            LOGGER.error("Failed to send notification for post creation: {}", e.getMessage(), e);
        }

        return saved;
    }

    private String normalizeCategory(String rawName) {
        String normalized = rawName.replaceAll("\\s+", " ").trim();
        if (normalized.equalsIgnoreCase("quote") || normalized.equalsIgnoreCase("qoute")) {
            return "Quote";
        } else if (normalized.equalsIgnoreCase("announcement")) {
            return "Announcement";
        } else if (normalized.equalsIgnoreCase("events") || normalized.equalsIgnoreCase("event")) {
            return "Events";
        } else {
            return normalized.substring(0, 1).toUpperCase() + normalized.substring(1);
        }
    }

    private List<String> getTargetSectionNames(CreatePostRequest request) {
        // Priority 1: Check new sectionNames list (MULTIPLE SECTIONS)
        if (request.getSectionNames() != null && !request.getSectionNames().isEmpty()) {
            LOGGER.info("Targeting {} specific sections: {}", request.getSectionNames().size(), request.getSectionNames());
            return request.getSectionNames();
        }

        // Priority 2: Check old sectionName (SINGLE SECTION - backward compatibility)
        if (request.getSectionName() != null && !request.getSectionName().trim().isEmpty()) {
            LOGGER.info("Targeting single section: {}", request.getSectionName());
            return Collections.singletonList(request.getSectionName().trim());
        }

        // Priority 3: Check old sectionId (SINGLE SECTION - backward compatibility)
        if (request.getSectionId() != null) {
            Optional<Section> section = sectionRepository.findById(request.getSectionId());
            if (section.isPresent()) {
                LOGGER.info("Targeting section by ID: {}", section.get().getSectionName());
                return Collections.singletonList(section.get().getSectionName());
            }
        }

        LOGGER.info("No specific sections targeted - will send to ALL students");
        return Collections.emptyList();
    }

    private void sendNotificationsForPost(Post post, List<String> targetSectionNames, String categoryName) {
        List<String> studentUserIds;

        if (targetSectionNames == null || targetSectionNames.isEmpty()) {
            studentUserIds = userRepository.findAllByRole(Role.STUDENT_ROLE.name())
                    .stream()
                    .map(User::getUserId)
                    .collect(Collectors.toList());
            LOGGER.info("Sending notification to ALL students ({} users)", studentUserIds.size());
        } else {
            Set<String> uniqueUserIds = new HashSet<>();

            for (String sectionName : targetSectionNames) {
                List<String> sectionUserIds = userRepository.findUserIdsBySectionNameAndRole(
                        sectionName,
                        Role.STUDENT_ROLE.name()
                );
                uniqueUserIds.addAll(sectionUserIds);
                LOGGER.info("Found {} students in section {}", sectionUserIds.size(), sectionName);
            }

            studentUserIds = new ArrayList<>(uniqueUserIds);
            LOGGER.info("Sending notification to {} students in {} section(s)",
                    studentUserIds.size(), targetSectionNames.size());
        }

        if (!studentUserIds.isEmpty()) {
            LOGGER.info("POST {} - Notifying user IDs: {}", post.getPostId(), studentUserIds);
            notificationService.sendNotificationToAllStudent(
                    studentUserIds,
                    "New Post from Guidance",
                    "A new " + categoryName.toLowerCase() + " has been posted. Check your Content Hub!",
                    "POST_UPDATE"
            );
        } else {
            LOGGER.warn("No students found to send notifications to");
        }
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
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new EntityNotFoundException("Student not found: " + studentId));

        User user = student.getUser();
        if (user == null) {
            LOGGER.warn("Student {} has no associated user", studentId);
            return Collections.emptyList();
        }

        String userId = user.getUserId();
        String sectionName = student.getSection() != null ? student.getSection().getSectionName() : null;

        String loginIdSql = "SELECT login_id FROM tbl_login WHERE user_id = ?";
        String loginId;
        try {
            loginId = jdbcTemplate.queryForObject(loginIdSql, String.class, userId);
        } catch (Exception e) {
            LOGGER.error("Could not find login_id for user_id: {}", userId);
            return Collections.emptyList();
        }

        // IMPROVED: More specific notification matching using message content
        String sql = "SELECT DISTINCT p.post_id, p.post_content, p.posted_date, c.category_name, " +
                "       s.section_name, s.organization, " +
                "       TRIM(COALESCE(per.first_name, '') || ' ' || COALESCE(per.last_name, '')) AS posted_by " +
                "FROM tbl_posts p " +
                "JOIN tbl_category c ON p.category_id = c.category_id " +
                "LEFT JOIN tbl_section s ON p.section_id = s.section_id " +
                "LEFT JOIN tbl_guidance_staff gs ON p.employee_number = gs.employee_number " +
                "LEFT JOIN tbl_person per ON gs.person_id = per.id " +
                "INNER JOIN tbl_notification n ON n.action_type = 'POST_UPDATE' " +
                "   AND n.user_id = ? " +
                "   AND ABS(EXTRACT(EPOCH FROM (p.posted_date - n.created_at))) < 10 " + // Within 10 seconds
                "WHERE UPPER(TRIM(c.category_name)) <> 'QUOTE' " +
                "ORDER BY p.posted_date DESC " +
                "LIMIT ?";

        List<Map<String, Object>> posts = jdbcTemplate.queryForList(sql, loginId, limit);

        LOGGER.info("Found {} posts for student {} (userId: {}, loginId: {}, section: {})",
                posts.size(), studentId, userId, loginId, sectionName);

        return posts;
    }

    @Override
    public List<Map<String, Object>> getPostsBySection(String sectionName, int limit) {
        if (sectionName == null || sectionName.trim().isEmpty()) {
            throw new IllegalArgumentException("Section name cannot be empty");
        }

        String trimmedSection = sectionName.trim();

        LOGGER.info("Fetching posts for section: {}", trimmedSection);

        String sql = "SELECT DISTINCT p.post_id, p.post_content, p.posted_date, c.category_name, " +
                "       TRIM(COALESCE(per.first_name, '') || ' ' || COALESCE(per.last_name, '')) AS posted_by, " +
                "       ? AS target_section, " +
                "       COUNT(DISTINCT n.user_id) AS student_count " +
                "FROM tbl_posts p " +
                "JOIN tbl_category c ON p.category_id = c.category_id " +
                "LEFT JOIN tbl_guidance_staff gs ON p.employee_number = gs.employee_number " +
                "LEFT JOIN tbl_person per ON gs.person_id = per.id " +
                "INNER JOIN tbl_notification n ON n.action_type = 'POST_UPDATE' " +
                "   AND n.created_at BETWEEN p.posted_date AND p.posted_date + INTERVAL '5 minutes' " +
                "INNER JOIN tbl_login l ON n.user_id = l.login_id " +
                "INNER JOIN tbl_user u ON l.user_id = u.user_id " +
                "INNER JOIN tbl_student st ON u.user_id = st.user_id " +
                "INNER JOIN tbl_section sec ON st.section_id = sec.section_id " +
                "WHERE UPPER(TRIM(c.category_name)) <> 'QUOTE' " +
                "  AND sec.section_name = ? " +
                "GROUP BY p.post_id, p.post_content, p.posted_date, c.category_name, " +
                "         per.first_name, per.last_name " +
                "ORDER BY p.posted_date DESC " +
                "LIMIT ?";

        List<Map<String, Object>> posts = jdbcTemplate.queryForList(sql, trimmedSection, trimmedSection, limit);

        LOGGER.info("Found {} posts targeted to section: {}", posts.size(), trimmedSection);

        return posts;
    }

    @Override
    public List<String> findStudentsSection() {
        return sectionRepository.findAllDistinctSectionName();
    }

    private Long getStudentSectionId(Long studentId) {
        Student student = studentRepository.findById(studentId).orElse(null);
        return student != null && student.getSection() != null ? student.getSection().getId() : null;
    }
}