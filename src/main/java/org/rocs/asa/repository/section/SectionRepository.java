package org.rocs.asa.repository.section;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;

@Repository
public class SectionRepository {
    private final JdbcTemplate jdbc;

    public SectionRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<Map<String, Object>> findDistinctCourses() {
        String sql = """
      SELECT DISTINCT course AS course
      FROM tbl_section
      WHERE course IS NOT NULL
      ORDER BY course
    """;
        return jdbc.queryForList(sql);
    }

    public List<Map<String, Object>> findDistinctClusters() {
        String sql = """
      SELECT DISTINCT cluster_name AS cluster_name
      FROM tbl_section
      WHERE cluster_name IS NOT NULL
      ORDER BY cluster_name
    """;
        return jdbc.queryForList(sql);
    }

}