package org.rocs.asa.repository.section;

import org.rocs.asa.domain.section.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SectionRepository extends JpaRepository<Section,Long> {

    @Query("SELECT DISTINCT s.sectionName FROM Section s ORDER BY s.sectionName ASC")
    List<String> findAllDistinctSectionName();
    @Query(value = "SELECT * FROM tbl_section WHERE section_name = :sectionName " +
            "LIMIT 1",
            nativeQuery = true)
    Optional<Section> findBySectionName(@Param("sectionName") String sectionName);
    /**
     * Find a section by its name and cluster head.
     * This prevents duplicate sections from being created.
     *
     * @param sectionName the name of the section (e.g., "IT-301")
     * @param clusterHead the cluster head assigned to this section
     * @return the existing Section if found, null otherwise
     */
    Section findBySectionNameAndClusterHead(String sectionName, String clusterHead);

    @Query("SELECT DISTINCT s.organization FROM Section s")
    List<String> findAllOrganization();

}