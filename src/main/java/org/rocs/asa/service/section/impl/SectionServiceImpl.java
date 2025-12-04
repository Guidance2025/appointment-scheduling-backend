package org.rocs.asa.service.section.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.rocs.asa.service.section.SectionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SectionServiceImpl implements SectionService {

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<String> listCourses() {
        return em.createNativeQuery("""
            select distinct course from tbl_section
            where course is not null
            order by course
        """).getResultList();
    }

    @Override
    public List<String> listClusters() {
        return em.createNativeQuery("""
            select distinct cluster_name from tbl_section
            where cluster_name is not null
            order by cluster_name
        """).getResultList();
    }
}
