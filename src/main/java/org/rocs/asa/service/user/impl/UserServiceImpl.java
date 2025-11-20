package org.rocs.asa.service.user.impl;

import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.rocs.asa.domain.guidance.staff.GuidanceStaff;
import org.rocs.asa.domain.person.Person;
import org.rocs.asa.domain.registration.Registration;
import org.rocs.asa.domain.section.Section;
import org.rocs.asa.domain.student.Student;
import org.rocs.asa.domain.user.User;
import org.rocs.asa.domain.user.principal.UserPrincipal;
import org.rocs.asa.exception.domain.*;
import org.rocs.asa.repository.guidance.staff.GuidanceStaffRepository;
import org.rocs.asa.repository.person.PersonRepository;
import org.rocs.asa.repository.section.SectionRepository;
import org.rocs.asa.repository.student.StudentRepository;
import org.rocs.asa.repository.user.UserRepository;
import org.rocs.asa.service.email.EmailService;
import org.rocs.asa.service.login.attempts.LoginAttemptService;
import org.rocs.asa.service.password.reset.PasswordResetTokenService;
import org.rocs.asa.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.rocs.asa.exception.constants.ExceptionConstants.USER_NOT_FOUND;
import static org.rocs.asa.utils.security.enumeration.Role.*;

@Service
@Transactional
@Qualifier(value = "userDetailsService")
public class UserServiceImpl implements UserService, UserDetailsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);
    private UserRepository userRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private LoginAttemptService loginAttemptsService;
    private StudentRepository studentRepository;
    private PersonRepository personRepository;
    private GuidanceStaffRepository guidanceStaffRepository;
    private EmailService emailService;
    private PasswordResetTokenService passwordResetTokenService;
    private SectionRepository sectionRepository;

    @Value("${spring.application.frontend-url}")
    private String frontendUrl;

    @Value("${spring.application.endpoints.password-reset-verify}")
    private String verifyEndpoint;
    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           BCryptPasswordEncoder bCryptPasswordEncoder,
                           LoginAttemptService loginAttemptsService,
                           StudentRepository studentRepository,
                           PersonRepository personRepository,
                           GuidanceStaffRepository guidanceStaffRepository,
                           EmailService emailService,
                           PasswordResetTokenService passwordResetTokenService,
                           SectionRepository sectionRepository) {

        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.loginAttemptsService = loginAttemptsService;
        this.studentRepository = studentRepository;
        this.personRepository = personRepository;
        this.guidanceStaffRepository = guidanceStaffRepository;
        this.emailService = emailService;
        this.passwordResetTokenService = passwordResetTokenService;
        this.sectionRepository = sectionRepository;
    }



    @Override
    public User findUserByUsername(String username) {
        return this.userRepository.findUserByUsername(username);
    }

    @Override
    public User findUserByPersonEmail(String email) {
        return this.userRepository.findUserByPersonEmail(email);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = this.findUserByUsername(username);
        if(user == null){
            LOGGER.info(USER_NOT_FOUND);
            throw new UserNotFoundException(USER_NOT_FOUND);
        }else{
            validateLoginAttempt(user);
            user.setLastLoginDate(new Date());
            this.userRepository.save(user);
            return new UserPrincipal(user);
        }
    }

    @Override
    public Registration registerUser(Registration registration) {
        if(registration.getStudent() != null){
            return registerStudent(registration);
        }else if(registration.getGuidanceStaff() != null) {
            return registerGuidanceStaff(registration);
        }
        return registration;
    }
    @Override
    public void initiatePasswordReset(String username, String newPassword) throws MessagingException {
        User existingUser = userRepository.findUserByUsername(username);
        if(existingUser == null ) {
            throw new UserNotFoundException("User does not exist");

        }
        String userEmail = existingUser.getPerson().getEmail();

        User validateEmail = userRepository.findUserByPersonEmail(userEmail);

        if(validateEmail == null) {
            throw new EmailNotFoundException("Email does not Exist");
        }
        if(this.passwordResetTokenService.exceedMaxAttempts(userEmail)) {
            throw new TooManyAttemptsException("Too many attempts. Try again later");
        }
        String encryptedPassword = encodePassword(newPassword);
        String token = passwordResetTokenService.generateSecureToken(userEmail,encryptedPassword);

        String verifyToken = buildResetPasswordUrl(token);
        this.passwordResetTokenService.incrementAttempts(userEmail);
        this.emailService.sendPasswordResetVerificationEmail(userEmail,verifyToken);
    }
    @Override
    public void verifyAndCompletePasswordReset(String token) {
        Map<String,String> tokenData = passwordResetTokenService.validateToken(token);
        String email = tokenData.get("email");
        String encryptedPassword = tokenData.get("password");

        if (email == null || encryptedPassword == null) {
            throw new InvalidTokenException("Invalid or expired token");
        }

        User existingUser = this.userRepository.findUserByPersonEmail(email);

        existingUser.setPassword(encryptedPassword);
        existingUser.setActive(true);
        existingUser.setLocked(false);
        userRepository.save(existingUser);

        this.passwordResetTokenService.evictTokenInCache(token);
        this.passwordResetTokenService.clearAttempts(email);


        LOGGER.info("Password successfully reset for user: {}", existingUser.getUsername());

    }
    private String buildResetPasswordUrl(String token){
        return frontendUrl + "/verification-success?token=" + token;
    }

    private Registration registerStudent(Registration registration){
        validateUsername(StringUtils.EMPTY, registration.getStudent().getUser().getUsername());

        String username = registration.getStudent().getUser().getUsername();
        String password = registration.getStudent().getUser().getPassword() == null
                ? generatePassword()
                : registration.getStudent().getUser().getPassword();
        Person savedPerson= this.personRepository.save(registration.getStudent().getPerson());

        User newUser = new User();
        newUser.setPerson(savedPerson);
        newUser.setUserId(generateUserId());
        newUser.setUsername(username);
        newUser.setPassword(encodePassword(password));
        newUser.setActive(true);
        newUser.setLocked(false);
        newUser.setJoinDate(new Date());
        newUser.setRole(STUDENT_ROLE.name());
        newUser.setAuthorities(Arrays.stream(STUDENT_ROLE.getAuthorities()).toList());

        User savedUser = this.userRepository.save(newUser);

        Section section = registration.getStudent().getSection();
        Section saveSection = this.sectionRepository.save(section);

        Student student = new Student();
        student.setPerson(savedPerson);
        student.setSection(saveSection);
        student.setStudentNumber(registration.getStudent().getStudentNumber());
        student.setUser(savedUser);

        Student savedStudent = this.studentRepository.save(student);
        Registration savedRegistration = new Registration();

        savedRegistration.setStudent(savedStudent);
        LOGGER.info("Guidance Staff Account Successfully Created! ");
        return savedRegistration;
    }


    private Registration registerGuidanceStaff(Registration registration) {

        validateUsername(StringUtils.EMPTY, registration.getGuidanceStaff().getUser().getUsername());
        String username = registration.getGuidanceStaff().getUser().getUsername();
        String password = registration.getGuidanceStaff().getUser().getPassword();

        Person savedPerson = this.personRepository.save(registration.getGuidanceStaff().getPerson());

        User newUser = new User();
        newUser.setPerson(savedPerson);
        newUser.setUserId(generateUserId());
        newUser.setUsername(username);
        newUser.setPassword(encodePassword(password));
        newUser.setActive(true);
        newUser.setLocked(false);
        newUser.setJoinDate(new Date());
        newUser.setRole(GUIDANCE_ROLE.name());
        newUser.setAuthorities(Arrays.stream(GUIDANCE_ROLE.getAuthorities()).toList());
        User savedUser = this.userRepository.save(newUser);

        GuidanceStaff guidanceStaff = new GuidanceStaff();
        guidanceStaff.setPerson(savedPerson);
        guidanceStaff.setUser(savedUser);
        guidanceStaff.setPositionInRc(registration.getGuidanceStaff().getPositionInRc());

        GuidanceStaff savedGuidanceStaff = guidanceStaffRepository.save(guidanceStaff);

        Registration savedRegistration = new Registration();
        savedRegistration.setGuidanceStaff(savedGuidanceStaff);
        LOGGER.info("Student Account Successfully Created! ");
        return savedRegistration;
    }

    @Override
    public Map<String, Object> buildLoginResponse(User user) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Login Success");
        response.put("role", user.getRole());
        response.put("userId", user.getUserId());

        if ("GUIDANCE_ROLE".equals(user.getRole())) {
            GuidanceStaff guidanceStaff = guidanceStaffRepository.findByUser(user);
            if (guidanceStaff != null) {
                response.put("guidanceStaffId", guidanceStaff.getId());
                LOGGER.info("Guidance Staff ID added to response: {}", guidanceStaff.getId());
            }
        }

        if ("STUDENT_ROLE".equals(user.getRole())) {
            Student student = studentRepository.findByUser(user);
            if (student != null) {
                response.put("studentId", student.getId());
                LOGGER.info("Student ID added to response: {}", student.getId());
            }
        }

        return response;
    }

    private User validateUsername(String currentUsername, String newUsername) throws UserNotFoundException,UsernameExistException{
        User userEmail = findUserByUsername(newUsername);
        if(StringUtils.isNotBlank(currentUsername)){
            User currentUser = findUserByUsername(currentUsername);
            if(currentUser == null){
                throw new UserNotFoundException("User not found");
            }
            if(userEmail != null && !userEmail.getId().equals(currentUser.getId())){
                throw new UsernameExistException("Username is already exist");
            }
            return currentUser;
        } else {
            if(userEmail != null){
                throw new UsernameExistException("Username is already exist");
            }
            return null;
        }

    }
    private String generateUserId(){
        return RandomStringUtils.randomNumeric(10);
    }
    private String generatePassword(){
        return RandomStringUtils.randomAlphanumeric(10);
    }
    private String encodePassword(String password){
        return bCryptPasswordEncoder.encode(password);
    }
    private void validateLoginAttempt(User user){
        if(!user.isLocked()){
            if(loginAttemptsService.hasExceedMaxAttempts(user.getUsername())){
                user.setLocked(true);
            }else{
                user.setLocked(false);
            }
        }else{
            loginAttemptsService.evictUserToLoginAttemptCache(user.getUsername());
        }
    }
}