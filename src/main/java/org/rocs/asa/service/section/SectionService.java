package org.rocs.asa.service.section;

import org.rocs.asa.repository.section.SectionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SectionService {

    private final SectionRepository sectionRepository;

    public SectionService(SectionRepository sectionRepository) {
        this.sectionRepository = sectionRepository;
    }

    public List<Map<String, Object>> listCourses() {
        return sectionRepository.findDistinctCourses();
    }

    public List<Map<String, Object>> listClusters() {
        return sectionRepository.findDistinctClusters();
    }
}

