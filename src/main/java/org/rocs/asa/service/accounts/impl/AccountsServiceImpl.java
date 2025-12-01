package org.rocs.asa.service.accounts.impl;

import jakarta.mail.MessagingException;
import org.rocs.asa.domain.account.dto.GuidanceStaffDto;
import org.rocs.asa.domain.account.dto.StudentAccountDto;
import org.rocs.asa.domain.guidance.staff.GuidanceStaff;
import org.rocs.asa.domain.person.Person;
import org.rocs.asa.domain.student.Student;
import org.rocs.asa.domain.user.User;
import org.rocs.asa.exception.domain.*;
import org.rocs.asa.repository.guidance.staff.GuidanceStaffRepository;
import org.rocs.asa.repository.student.StudentRepository;
import org.rocs.asa.repository.user.UserRepository;
import org.rocs.asa.service.accounts.AccountsService;
import org.rocs.asa.service.email.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
@Service
public class AccountsServiceImpl implements AccountsService {
    private static Logger LOGGER = LoggerFactory.getLogger(AccountsServiceImpl.class);
    private UserRepository userRepository;
    private GuidanceStaffRepository guidanceStaffRepository;
    private StudentRepository studentRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private EmailService emailService;

    @Autowired
    public AccountsServiceImpl(UserRepository userRepository,
                               GuidanceStaffRepository guidanceStaffRepository,
                               StudentRepository studentRepository,
                               BCryptPasswordEncoder bCryptPasswordEncoder,
                               EmailService emailService
    ) {
        this.userRepository = userRepository;
        this.guidanceStaffRepository = guidanceStaffRepository;
        this.studentRepository = studentRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.emailService = emailService;
    }



    @Override
    public List<User> getAllAccounts() {
        return  userRepository.findAll();
    }

    @Override
    public List<GuidanceStaffDto> getGuidanceStaffAccount() {
        List <GuidanceStaff> guidanceStaffs = guidanceStaffRepository.findAll();
        List<GuidanceStaffDto> guidanceStaffDto = new ArrayList<>();

        for(GuidanceStaff staff : guidanceStaffs) {
                GuidanceStaffDto dto = new GuidanceStaffDto();

                dto.setEmployeeNumber(staff.getId());
                dto.setFirstname(staff.getPerson().getFirstName());
                dto.setLastname(staff.getPerson().getLastName());
                dto.setPositionInRc(staff.getPositionInRc());
                dto.setUsername(staff.getUser().getUsername());
                dto.setPassword(staff.getUser().getPassword());
                dto.setJoinDate(staff.getUser().getJoinDate());
                dto.setRole(staff.getUser().getRole());
                dto.setIsActive(staff.getUser().isActive());
                dto.setIsLocked(staff.getUser().isLocked());

                guidanceStaffDto.add(dto);
            }

        return guidanceStaffDto;
    }

    @Override
    public List<StudentAccountDto> getStudentAccount() {
        List <StudentAccountDto> studentAccount = new ArrayList<>();
        List<Student> studentList = studentRepository.findAll();

        for (Student student  : studentList) {
                StudentAccountDto dto = new StudentAccountDto();
                dto.setStudentNumber(student.getStudentNumber());
                dto.setFirstname(student.getPerson().getFirstName());
                dto.setMiddlename(student.getPerson().getMiddleName());
                dto.setLastname(student.getPerson().getLastName());
                dto.setUsername(student.getUser().getUsername());
                dto.setPassword(student.getUser().getPassword());
                dto.setJoinDate(student.getUser().getJoinDate());
                dto.setIsActive(student.getUser().isActive());
                dto.setIsLocked(student.getUser().isLocked());
                dto.setRole(student.getUser().getRole());
                studentAccount.add(dto);

        }
        return studentAccount;
    }

    @Override
    public void softDeleteStudentAccount(String studentNumber) {
        User studentUser = getUserByStudentNumber(studentNumber);
        validateStudentNumber(studentNumber);
        if(studentUser.isLocked()) {
            LOGGER.info("Cannot Delete Student Locked Account.");
            throw new LockedException("Account is Locked Cannot Delete");
        }
        studentUser.setActive(false);
        studentUser.setLocked(true);
        userRepository.save(studentUser);

    }

    @Override
    public void softDeleteEmployeeAccount(Long id) {
        GuidanceStaff guidanceStaff = guidanceStaffRepository.findById(id).orElseThrow(()
                -> new GuidanceStaffNotFoundException("GuidanceStaff Not Found"));
        User staffUser = guidanceStaff.getUser();
        if(staffUser.isLocked()) {
           LOGGER.info("Cannot Delete Guidance Staff Locked Account.");
           throw new LockedException("Account is Locked Cannot Delete");
        }
        staffUser.setActive(false);
        staffUser.setLocked(true);
        userRepository.save(staffUser);
        LOGGER.info("Successfully Deleted ");
    }

    @Override
    @Transactional
    public void updateStudentCredentials(String studentNumber, String newPassword, Boolean isLocked) throws MessagingException {

        LOGGER.info("Updating credentials for student: {}", studentNumber);

        validateStudentNumber(studentNumber);

        User user = getUserByStudentNumber(studentNumber);

        boolean passwordChanged = false;

        if (newPassword != null && !newPassword.isBlank()) {
            validatePassword(newPassword);
            user.setPassword(bCryptPasswordEncoder.encode(newPassword));
            passwordChanged = true;
            LOGGER.info("Password updated for student: {}", studentNumber);
        }

        if (isLocked != null) {
            user.setLocked(isLocked);
            user.setActive(!isLocked);
            LOGGER.info("Student Account Locked : {}", isLocked);
            LOGGER.info("Student Account Active Status : {}", !isLocked);
        }

        userRepository.save(user);

        if (passwordChanged) {
            emailService.sendStudentNewPasswordEmail(
                    user.getPerson().getEmail(),
                    newPassword
            );
        }

        LOGGER.info("Student credentials updated successfully for: {}", studentNumber);
    }


    @Override
    public void updateGuidanceEmployeeCredentials(Long id, String email, Boolean isLocked) {
        LOGGER.info("Updating credentials for guidance staff: {}", id);
        GuidanceStaff guidanceStaff = guidanceStaffRepository.findById(id)
                .orElseThrow(() -> new GuidanceStaffNotFoundException("Employee Not Found"));

        User user = guidanceStaff.getUser();
        Person userGuidanceDetails = guidanceStaff.getPerson();
        if(email != null || email.isBlank()) {
            userGuidanceDetails.setEmail(email);
            LOGGER.info("Email Updated Successfully for Guidance Employee");
        }
        if(isLocked != null) {
            user.setLocked(isLocked);
            user.setActive(!isLocked);
            LOGGER.info("Guidance Staff Account Locked : {}", isLocked);
            LOGGER.info("Guidance Staff Account Active Status : {}", !isLocked);
        }
        userRepository.save(user);
        LOGGER.info("Guidance credentials updated successfully for : {}"
                ,userGuidanceDetails.getFirstName() + userGuidanceDetails.getLastName());
    }

    private void validateStudentNumber(String studentNumber) {
        if (studentNumber == null || studentNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Student number cannot be null or empty");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 6) {
            throw new InvalidPasswordException("Password must be at least 6 characters");
        }
    }
    private User getUserByStudentNumber(String studentNumber) {
        Student student = studentRepository.findStudentByStudentNumber(studentNumber);
        if(student == null) {
            throw new StudentNotFoundException("Student not found: " + studentNumber);
        }
        User user = student.getUser();
        if (user == null) {
            LOGGER.info("No user found , with this student Number {}",studentNumber);
            throw new UserNotFoundException("No user associated with student: " + studentNumber);
        }
        return user;
    }
}