package org.rocs.asa.service.student.inforamation.impl;

import org.rocs.asa.domain.section.Section;
import org.rocs.asa.domain.student.information.response.StudentDetailsResponse;
import org.rocs.asa.domain.student.Student;
import org.rocs.asa.repository.section.SectionRepository;
import org.rocs.asa.repository.student.StudentRepository;
import org.rocs.asa.service.student.inforamation.StudentInformationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class StudentInformationServiceImpl implements StudentInformationService {
     private StudentRepository studentRepository;
     private SectionRepository sectionRepository;

    @Autowired
    public StudentInformationServiceImpl(StudentRepository studentRepository,SectionRepository sectionRepository) {
        this.studentRepository = studentRepository;
        this.sectionRepository = sectionRepository;
    }

    @Override
    public List<StudentDetailsResponse> getAllStudent() {
        List<Student> student = studentRepository.findAll();
        return student.stream()
                .map(students-> {
                    StudentDetailsResponse info = new StudentDetailsResponse();
                    info.setStudentNumber(students.getStudentNumber());
                    info.setPerson(students.getPerson());
                    info.setSection(students.getSection());
                    return info;
                })
                .toList();
    }

    @Override
    public List<String> getAllOrganization() {
        return sectionRepository.findAllOrganization();
    }
}
