package org.rocs.asa.controller.section;

import org.rocs.asa.service.section.SectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sections")
public class SectionController {

    private final SectionService sectionService;

    public SectionController(SectionService sectionService) {
        this.sectionService = sectionService;
    }

    @GetMapping("/courses")
    public ResponseEntity<List<String>> courses() {
        return ResponseEntity.ok(sectionService.listCourses());
    }

    @GetMapping("/clusters")
    public ResponseEntity<List<String>> clusters() {
        return ResponseEntity.ok(sectionService.listClusters());
    }
}
