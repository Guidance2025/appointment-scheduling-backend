package org.rocs.asa.service.post.impl;

import jakarta.persistence.EntityNotFoundException;
import org.rocs.asa.domain.category.Category;
import org.rocs.asa.domain.guidance.staff.GuidanceStaff;
import org.rocs.asa.domain.post.Post;
import org.rocs.asa.dto.CreatePostRequest;
import org.rocs.asa.repository.category.CategoryRepository;
import org.rocs.asa.repository.post.PostRepository;
import org.rocs.asa.service.guidance.GuidanceService;
import org.rocs.asa.service.post.PostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PostServiceImpl implements PostService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostServiceImpl.class);

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final GuidanceService guidanceService;
    private final JdbcTemplate jdbcTemplate;

    public PostServiceImpl(PostRepository postRepository,
                           CategoryRepository categoryRepository,
                           GuidanceService guidanceService,
                           JdbcTemplate jdbcTemplate) {
        this.postRepository = postRepository;
        this.categoryRepository = categoryRepository;
        this.guidanceService = guidanceService;
        this.jdbcTemplate = jdbcTemplate;
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

        Integer exists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) " +
                        "FROM tbl_posts p " +
                        "JOIN tbl_category c ON p.category_id = c.category_id " +
                        "WHERE p.employee_number = ? " +
                        "  AND NVL(p.section_id, -1) = NVL(?, -1) " +
                        "  AND p.post_content = ? " +
                        "  AND UPPER(TRIM(c.category_name)) = UPPER(TRIM(?)) " +
                        "  AND p.posted_date >= SYSTIMESTAMP - INTERVAL '5' SECOND",
                Integer.class,
                employeeNumber, request.getSectionId(), content, capped64
        );

        if (exists != null && exists > 0) {
            LOGGER.warn("Duplicate create ignored (emp={}, catName='{}', section={}, content='{}')",
                    employeeNumber, capped64, request.getSectionId(), content);

            // Optionally return the latest matching post so caller still gets something
            List<Long> ids = jdbcTemplate.queryForList(
                    "SELECT p.post_id " +
                            "FROM tbl_posts p " +
                            "JOIN tbl_category c ON p.category_id = c.category_id " +
                            "WHERE p.employee_number = ? " +
                            "  AND NVL(p.section_id, -1) = NVL(?, -1) " +
                            "  AND p.post_content = ? " +
                            "  AND UPPER(TRIM(c.category_name)) = UPPER(TRIM(?)) " +
                            "ORDER BY p.posted_date DESC FETCH FIRST 1 ROW ONLY",
                    Long.class,
                    employeeNumber, request.getSectionId(), content, capped64
            );
            if (!ids.isEmpty()) {
                return postRepository.findById(ids.get(0)).orElse(null);
            }
        }

        Category toSave = new Category();
        toSave.setCategoryName(capped64);
        Category savedCategory = categoryRepository.save(toSave);

        Post post = new Post();
        post.setEmployeeNumber(employeeNumber);
        post.setCategoryId(savedCategory.getCategoryId());
        post.setSectionId(request.getSectionId());
        post.setPostContent(content);
        post.setPostedDate(LocalDateTime.now());

        Post saved = postRepository.save(post);
        LOGGER.info("Post created id={} by employeeNumber={} with NEW category id={} name='{}'",
                saved.getPostId(), employeeNumber, savedCategory.getCategoryId(), savedCategory.getCategoryName());
        return saved;
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
                        "      TRIM(NVL(per.first_name, '') || ' ' || NVL(per.last_name, '')) AS posted_by, " +
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
                        ") WHERE ROWNUM <= ?";

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
                        "  AND TRUNC(p.posted_date) = TRUNC(SYSDATE) " +
                        "ORDER BY p.posted_date DESC " +
                        "FETCH FIRST 1 ROW ONLY";
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
                            "FETCH FIRST 1 ROW ONLY";
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
                        "      TRIM(NVL(per.first_name, '') || ' ' || NVL(per.last_name, '')) AS posted_by, " +
                        "      ROW_NUMBER() OVER (PARTITION BY p.post_id ORDER BY p.posted_date DESC) AS rn " +
                        "    FROM tbl_posts p " +
                        "    JOIN tbl_category c ON p.category_id = c.category_id " +
                        "    LEFT JOIN tbl_section s ON p.section_id = s.section_id " +
                        "    LEFT JOIN tbl_guidance_staff gs ON p.employee_number = gs.employee_number " +
                        "    LEFT JOIN tbl_person per ON gs.person_id = per.id " +
                        "    WHERE UPPER(TRIM(c.category_name)) <> 'QUOTE' " +
                        "  ) t " +
                        "  WHERE t.rn = 1 " +           // one row per post_id
                        "  ORDER BY t.posted_date DESC " +
                        ") WHERE ROWNUM <= ?";

        String quoteTodaySql =
                "SELECT p.post_id, p.post_content, p.posted_date, s.section_name, s.organization " +
                        "FROM tbl_posts p " +
                        "JOIN tbl_category c ON p.category_id = c.category_id " +
                        "LEFT JOIN tbl_section s ON p.section_id = s.section_id " +
                        "WHERE UPPER(TRIM(c.category_name)) = 'QUOTE' " +
                        "  AND TRUNC(p.posted_date) = TRUNC(SYSDATE) " +
                        "ORDER BY p.posted_date DESC " +
                        "FETCH FIRST 1 ROW ONLY";

        String quoteLatestSql =
                "SELECT p.post_id, p.post_content, p.posted_date, s.section_name, s.organization " +
                        "FROM tbl_posts p " +
                        "JOIN tbl_category c ON p.category_id = c.category_id " +
                        "LEFT JOIN tbl_section s ON p.section_id = s.section_id " +
                        "WHERE UPPER(TRIM(c.category_name)) = 'QUOTE' " +
                        "ORDER BY p.posted_date DESC " +
                        "FETCH FIRST 1 ROW ONLY";

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
}
