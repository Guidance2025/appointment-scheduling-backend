package org.rocs.asa.controller.section;

import org.rocs.asa.service.section.SectionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sections")
public class SectionController {

    private final SectionService sectionService;

    public SectionController(SectionService sectionService) {
        this.sectionService = sectionService;
    }

    @GetMapping("/courses")
    public List<Map<String, Object>> courses() {
        return sectionService.listCourses();
    }

    @GetMapping("/clusters")
    public List<Map<String, Object>> clusters() {
        return sectionService.listClusters();
    }
}

