package org.rocs.asa.repository.post;

import org.rocs.asa.domain.post.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByGuidanceStaff_EmployeeNumber(Long employeeNumber);
    List<Post> findBySection_SectionId(Long sectionId);
    List<Post> findByCategory_CategoryId(Long categoryId);
}
