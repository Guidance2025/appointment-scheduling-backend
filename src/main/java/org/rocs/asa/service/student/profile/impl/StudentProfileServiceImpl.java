package org.rocs.asa.service.student.profile.impl;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.rocs.asa.domain.person.Person;
import org.rocs.asa.domain.section.Section;
import org.rocs.asa.domain.student.Student;
import org.rocs.asa.exception.domain.EmptyFieldException;
import org.rocs.asa.exception.domain.StudentNotFoundException;
import org.rocs.asa.exception.domain.StudentNumberAlreadyExistException;
import org.rocs.asa.repository.student.StudentRepository;
import org.rocs.asa.service.student.profile.StudentProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StudentProfileServiceImpl implements StudentProfileService {
    private static Logger LOGGER = LoggerFactory.getLogger(StudentProfileServiceImpl.class);

    private StudentRepository studentRepository;

    @Autowired
    public StudentProfileServiceImpl(StudentRepository studentRepository) {
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

    @Override
    @Transactional
    public Student saveStudentProfile( Student student) {
        boolean exists = studentRepository.existsByStudentNumber(student.getStudentNumber());
        if(exists) {
            LOGGER.error("Student Number Already Exist {}", student.getStudentNumber());
            throw new StudentNumberAlreadyExistException("Student Number Already Exist");
        }

        validateEmptyField(student);

        Student newStudent = new Student();
        newStudent.setStudentNumber(student.getStudentNumber());

        Person person = new Person();
        person.setFirstName(student.getPerson().getFirstName());
        person.setLastName(student.getPerson().getLastName());
        person.setAge(student.getPerson().getAge());
        person.setBirthdate(student.getPerson().getBirthdate());
        person.setGender(student.getPerson().getGender());
        person.setEmail(student.getPerson().getEmail());
        person.setAddress(student.getPerson().getAddress());
        person.setContactNumber(student.getPerson().getContactNumber());


        Section section = new Section();
        section.setSectionName(student.getSection().getSectionName());
        section.setOrganization(student.getSection().getOrganization());
        section.setClusterName(student.getSection().getClusterName());
        section.setSectionName(student.getSection().getSectionName());
        section.setClusterHead(student.getSection().getClusterHead());
        section.setCourse(student.getSection().getCourse());

        newStudent.setPerson(person);
        newStudent.setSection(section);

        return studentRepository.save(newStudent);
    }

    private void validateEmptyField(Student student){

        if (student.getStudentNumber() == null ||student.getStudentNumber().isEmpty()){
            LOGGER.error("Student Number cannot be empty");
            throw new EmptyFieldException("Student Number cannot be empty");
        }
        if (student.getPerson().getFirstName() == null || student.getPerson().getFirstName().isEmpty()) {
            LOGGER.error("Firstname cannot be empty");
            throw new EmptyFieldException("Firstname cannot be empty");
        }
        if (student.getPerson().getLastName() == null || student.getPerson().getLastName().isEmpty()) {
            LOGGER.error("Lastname cannot be empty");
            throw new EmptyFieldException("Lastname cannot be empty");
        }
        if (student.getPerson().getAge() == null) {
            LOGGER.error("Age cannot be empty");
            throw new EmptyFieldException("Age cannot be empty");
        }
        if (student.getPerson().getBirthdate() == null) {
            LOGGER.error("Birthday cannot be empty");
            throw new EmptyFieldException("Birthday cannot be empty");
        }
        if (student.getPerson().getEmail() == null || student.getPerson().getEmail().isEmpty()) {
            LOGGER.error("Email cannot be empty");
            throw new EmptyFieldException("Email cannot be empty");
        }
        if (student.getPerson().getAddress() == null || student.getPerson().getAddress().isEmpty()) {
            LOGGER.error("Address cannot be empty");
            throw new EmptyFieldException("Address cannot be empty");
        }
        if (student.getPerson().getContactNumber() == null || student.getPerson().getContactNumber().isEmpty()) {
            LOGGER.error("Contact Number cannot be empty");
            throw new EmptyFieldException("Contact Number cannot be empty");
        }
        if (student.getSection().getOrganization() == null || student.getSection().getOrganization().isEmpty()) {
            LOGGER.error("Organization cannot be empty");

            throw new EmptyFieldException("Organization cannot be empty");
        }
        if (student.getSection().getClusterName() == null || student.getSection().getClusterName().isEmpty()) {
            LOGGER.error("Cluster Name cannot be empty");

            throw new EmptyFieldException("Cluster Name  cannot be empty");
        }
        if (student.getSection().getSectionName() == null || student.getSection().getSectionName().isEmpty()) {
            LOGGER.error("Section Name cannot be empty");

            throw new EmptyFieldException("Section Name cannot be empty");
        }
        if (student.getSection().getClusterHead() == null || student.getSection().getClusterHead().isEmpty()) {
            LOGGER.error("Cluster Head cannot be empty");
            throw new EmptyFieldException("Cluster Head cannot be empty");

        }
        if (student.getSection().getCourse() == null || student.getSection().getCourse().isEmpty()) {
            LOGGER.error("Course  cannot be empty");
            throw new EmptyFieldException("Course cannot be empty");
        }

    }
}