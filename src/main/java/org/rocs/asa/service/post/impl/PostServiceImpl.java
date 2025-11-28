// src/main/java/org/rocs/asa/service/post/impl/PostServiceImpl.java
package org.rocs.asa.service.post.impl;

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

        // Normalize to canonical names for consistent routing
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

        Category toSave = new Category();
        toSave.setCategoryName(capped64);
        Category savedCategory = categoryRepository.save(toSave); // always inserts a new category row

        if (content.length() > 500) content = content.substring(0, 500);

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
                "SELECT * FROM (" +
                        "  SELECT " +
                        "    p.post_id, " +
                        "    p.post_content, " +
                        "    p.posted_date, " +
                        "    c.category_name, " +
                        "    s.section_name, " +
                        "    s.organization, " +
                        "    TRIM(NVL(per.first_name, '') || ' ' || NVL(per.last_name, '')) AS posted_by " +
                        "  FROM tbl_posts p " +
                        "  JOIN tbl_category c ON p.category_id = c.category_id " +
                        "  LEFT JOIN tbl_section s ON p.section_id = s.section_id " +
                        "  LEFT JOIN tbl_guidance_staff gs ON p.employee_number = gs.employee_number " +
                        "  LEFT JOIN tbl_person per ON gs.person_id = per.id " +
                        "  WHERE UPPER(TRIM(c.category_name)) <> 'QUOTE' " + // exclude Quote from feed
                        "  ORDER BY p.posted_date DESC " +
                        ") WHERE ROWNUM <= ?";

        return jdbcTemplate.queryForList(sql, limit);
    }

    @Override
    public Map<String, Object> getQuoteOfTheDay() {
        // Try today's quote WITH section data
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
            // Fallback: latest Quote overall WITH section data
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
                "SELECT * FROM (" +
                        "  SELECT " +
                        "    p.post_id, " +
                        "    p.post_content, " +
                        "    p.posted_date, " +
                        "    c.category_name, " +
                        "    s.section_name, " +
                        "    s.organization, " +
                        "    TRIM(NVL(per.first_name, '') || ' ' || NVL(per.last_name, '')) AS posted_by " +
                        "  FROM tbl_posts p " +
                        "  JOIN tbl_category c ON p.category_id = c.category_id " +
                        "  LEFT JOIN tbl_section s ON p.section_id = s.section_id " +
                        "  LEFT JOIN tbl_guidance_staff gs ON p.employee_number = gs.employee_number " +
                        "  LEFT JOIN tbl_person per ON gs.person_id = per.id " +
                        "  WHERE UPPER(TRIM(c.category_name)) <> 'QUOTE' " +
                        "  ORDER BY p.posted_date DESC " +
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
                payload.put("quote", new HashMap<>()); // no quote at all
            }
        }

        return payload;
    }
}
