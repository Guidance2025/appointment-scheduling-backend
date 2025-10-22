package org.rocs.asa.domain.section;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "tbl_section")
public class Section{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "section_id")
    private Long id;

    private String organization;

    @Column(name = "cluster_name")
    private String clusterName;

    @Column(name = "cluster_head")
    private String clusterHead;

    @Column(name = "section_name")
    private String sectionName;

    private String course;

}
