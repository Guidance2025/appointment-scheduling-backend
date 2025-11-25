package org.rocs.asa.service.student.impl;

import org.rocs.asa.domain.student.Student;
import org.rocs.asa.domain.user.User;
import org.rocs.asa.exception.domain.GuidanceStaffNotFoundException;
import org.rocs.asa.repository.student.StudentRepository;
import org.rocs.asa.service.student.StudentService;
import org.rocs.asa.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class StudentServiceImpl implements StudentService {
    private static Logger LOGGER = LoggerFactory.getLogger(StudentServiceImpl.class);
    private UserService userService;
    private StudentRepository studentRepository;
    @Autowired
    public StudentServiceImpl(UserService userService, StudentRepository studentRepository) {
        this.userService = userService;
        this.studentRepository = studentRepository;
    }
    @Override
    public Student findByAuthenticatedStudent() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User loggedInUser = userService.findUserByUsername(username);
        if(loggedInUser == null || loggedInUser.isLocked()) {
            LOGGER.info("Guidance Staff Not Yet Registered ");
            throw new GuidanceStaffNotFoundException("Student Not Found");
        }
        Student student = findByUser(loggedInUser);
        if (student == null){
            throw new GuidanceStaffNotFoundException("Student Required");
        }
        return student;
    }

    @Override
    public Student findByUser(User user) {
        return studentRepository.findByUser(user);
    }
}
