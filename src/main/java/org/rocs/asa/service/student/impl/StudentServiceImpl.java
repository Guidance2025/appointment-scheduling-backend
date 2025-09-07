package org.rocs.asa.service.student.impl;

import org.rocs.asa.domain.person.student.Student;
import org.rocs.asa.exception.domain.StudentNotFoundException;
import org.rocs.asa.repository.student.StudentRepository;
import org.rocs.asa.service.student.StudentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StudentServiceImpl implements StudentService {
    private static Logger LOGGER = LoggerFactory.getLogger(StudentServiceImpl.class);

    private StudentRepository studentRepository;

    @Autowired
    public StudentServiceImpl(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }
    public Student findStudentByStudentNumber(String studentNumber) {

        Student student = studentRepository.findStudentByStudentNumber(studentNumber);

        if (student == null) {
            LOGGER.error("Student with number [{}] not found", studentNumber);
            throw new StudentNotFoundException("Student with number " + studentNumber + " not found");
        }

        LOGGER.info("Successfully found student with number [{}]", studentNumber);
        return student;
    }
}