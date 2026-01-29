package org.rocs.asa.service.user.impl;

import jakarta.mail.MessagingException;
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
import org.rocs.asa.utils.security.enumeration.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.rocs.asa.exception.constants.ExceptionConstants.USER_NOT_FOUND;
import static org.rocs.asa.utils.security.enumeration.Role.*;

@Service
@Transactional
@Qualifier("userDetailsService")
public class UserServiceImpl implements UserService, UserDetailsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final LoginAttemptService loginAttemptsService;
    private final StudentRepository studentRepository;
    private final PersonRepository personRepository;
    private final GuidanceStaffRepository guidanceStaffRepository;
    private final EmailService emailService;
    private final PasswordResetTokenService passwordResetTokenService;
    private final SectionRepository sectionRepository;

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

    /**
     * Utility to normalize username consistently
     */
    private String normalizeUsername(String username) {
        return username == null ? null : username.toLowerCase().trim();
    }

    @Override
    public User findUserByUsername(String username) {
        return this.userRepository.findUserByUsername(normalizeUsername(username));
    }

    @Override
    public User findUserByPersonEmail(String email) {
        return this.userRepository.findUserByPersonEmail(email.trim().toLowerCase());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String normalizedUsername = normalizeUsername(username);
        LOGGER.info("Login attempt for username: {}", normalizedUsername);

        if (loginAttemptsService.hasExceedMaxAttempts(normalizedUsername)) {
            LOGGER.warn("Account locked due to too many failed attempts: {}", normalizedUsername);
            throw new LockedException("Your account has been locked");
        }

        User user = findUserByUsername(normalizedUsername);
        if (user == null) {
            LOGGER.error("User not found: {}", normalizedUsername);
            throw new UsernameNotFoundException(USER_NOT_FOUND);
        }

        if (user.isLocked()) {
            LOGGER.warn("Account is locked: {}", normalizedUsername);
            throw new LockedException("Your account has been locked");
        }

        user.setLastLoginDate(new Date());
        this.userRepository.save(user);

        return new UserPrincipal(user);
    }

    @Override
    public Registration registerUser(Registration registration) throws MessagingException {
        if (registration.getStudent() != null) {
            return registerStudent(registration);
        } else if (registration.getGuidanceStaff() != null) {
            return registerGuidanceStaff(registration);
        }
        return registration;
    }

    @Transactional
    private Registration registerStudent(Registration registration) throws MessagingException {
        if (registration == null || registration.getStudent() == null)
            throw new IllegalArgumentException("Registration and Student are required");

        Person person = registration.getStudent().getPerson();
        Section section = registration.getStudent().getSection();
        String studentNumber = registration.getStudent().getStudentNumber();

        if (person == null) throw new PersonNotFoundException("Person is required");
        if (section == null) throw new SectionNotFoundException("Section is required");
        if (studentNumber == null || studentNumber.trim().isEmpty())
            throw new IllegalArgumentException("Student number is required");

        String email = person.getEmail().trim().toLowerCase();

        if (findUserByPersonEmail(email) != null)
            throw new EmailAlreadyExistException("Email already exists");
        if (studentRepository.findStudentByStudentNumber(studentNumber) != null)
            throw new StudentNumberAlreadyExistException("Student number already exists");

        String username = normalizeUsername(studentNumber.replace("-", ""));

        String firstname = normalizeUsername(person.getFirstName());
        String lastname = normalizeUsername(person.getLastName());
        String lastFourDigits = studentNumber.substring(Math.max(0, studentNumber.length() - 4));

        String password = firstname.substring(0, Math.min(3, firstname.length())) +
                lastname.substring(0, Math.min(3, lastname.length())) +
                lastFourDigits;

        LOGGER.info("===================================");
        LOGGER.info("Student Account : ");
        LOGGER.info("Username: {}", username);
        LOGGER.info("Password: {}", password);
        LOGGER.info("===================================");

        validatePassword(password);

        person.setEmail(email);
        Person savedPerson = personRepository.save(person);

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
        User savedUser = userRepository.save(newUser);

        String sectionName = section.getSectionName().trim();
        String clusterHead = section.getClusterHead().trim();

        Section savedSection;

        Section existingSection = sectionRepository.findBySectionNameAndClusterHead(sectionName, clusterHead);

        if (existingSection != null) {
            savedSection = existingSection;
            LOGGER.info("Reusing existing section: {} with cluster head: {}", sectionName, clusterHead);
        } else {
            // Section doesn't exist - create new one
            section.setOrganization(determineOrganization(sectionName));
            section.setClusterName(determineClusterName(section.getOrganization(), sectionName));
            section.setCourse(determineCourse(sectionName));
            savedSection = sectionRepository.save(section);
            LOGGER.info("Created new section: {} with cluster head: {}", sectionName, clusterHead);
        }

        Student student = new Student();
        student.setPerson(savedPerson);
        student.setSection(savedSection);
        student.setStudentNumber(studentNumber);
        student.setUser(savedUser);
        Student savedStudent = studentRepository.save(student);

        Registration savedRegistration = new Registration();
        savedRegistration.setStudent(savedStudent);

        emailService.sendNewRegisterAccountEmail(email, username, password);

        LOGGER.info("Student account created: {} / {}", username, password);
        return savedRegistration;
    }

    @Transactional
    private Registration registerGuidanceStaff(Registration registration) throws MessagingException {
        if (registration == null || registration.getGuidanceStaff() == null)
            throw new IllegalArgumentException("Registration and GuidanceStaff are required");

        Person person = registration.getGuidanceStaff().getPerson();
        if (person == null) throw new PersonNotFoundException("Person is required");

        String email = person.getEmail().trim().toLowerCase();
        if (findUserByPersonEmail(email) != null)
            throw new EmailAlreadyExistException("Email already exists");

        String username = normalizeUsername(email.split("@")[0]);

        if (findUserByUsername(username) != null) {
            username = username + RandomStringUtils.randomNumeric(3);
        }

        String firstname = normalizeUsername(person.getFirstName());
        String lastname = normalizeUsername(person.getLastName());
        String password = firstname.substring(0, Math.min(3, firstname.length())) +
                lastname.substring(0, Math.min(3, lastname.length())) +
                RandomStringUtils.randomNumeric(4);
        LOGGER.info("===================================");
        LOGGER.info("Username " + username);
        LOGGER.info("Password " + password);
        LOGGER.info("===================================");
        validatePassword(password);

        person.setEmail(email);
        Person savedPerson = personRepository.save(person);

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
        User savedUser = userRepository.save(newUser);

        GuidanceStaff guidanceStaff = new GuidanceStaff();
        guidanceStaff.setPerson(savedPerson);
        guidanceStaff.setUser(savedUser);
        guidanceStaff.setPositionInRc(registration.getGuidanceStaff().getPositionInRc());
        GuidanceStaff savedGuidanceStaff = guidanceStaffRepository.save(guidanceStaff);

        Registration savedRegistration = new Registration();
        savedRegistration.setGuidanceStaff(savedGuidanceStaff);

        emailService.sendNewRegisterAccountEmail(email, username, password);

        LOGGER.info("GuidanceStaff account created - Username: {}", username);
        return savedRegistration;
    }

    @Override
    public void initiatePasswordReset(String username, String newPassword) throws MessagingException {
        String normalizedUsername = normalizeUsername(username);
        User existingUser = findUserByUsername(normalizedUsername);
        if (existingUser == null) throw new UserNotFoundException("User not found");

        String email = existingUser.getPerson().getEmail();
        if (passwordResetTokenService.exceedMaxAttempts(email)) throw new TooManyAttemptsException("Too many attempts");

        String encryptedPassword = encodePassword(newPassword);
        String token = passwordResetTokenService.generateSecureToken(email, encryptedPassword);
        String verifyUrl = frontendUrl + "/verification-success?token=" + token;
        passwordResetTokenService.incrementAttempts(email);

        emailService.sendPasswordResetVerificationEmail(email, verifyUrl);
    }

    @Override
    public void verifyAndCompletePasswordReset(String token) {
        Map<String, String> tokenData = passwordResetTokenService.validateToken(token);
        String email = tokenData.get("email");
        String encryptedPassword = tokenData.get("password");

        if (email == null || encryptedPassword == null) throw new InvalidTokenException("Invalid token");

        User user = findUserByPersonEmail(email);
        user.setPassword(encryptedPassword);
        user.setActive(true);
        user.setLocked(false);
        userRepository.save(user);

        passwordResetTokenService.evictTokenInCache(token);
        passwordResetTokenService.clearAttempts(email);
        loginAttemptsService.evictUserToLoginAttemptCache(user.getUsername());
    }

    @Override
    public Map<String, Object> buildLoginResponse(User user) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Login Success");
        response.put("role", user.getRole());
        response.put("userId", user.getUserId());

        if (GUIDANCE_ROLE.name().equals(user.getRole())) {
            GuidanceStaff guidanceStaff = guidanceStaffRepository.findByUser(user);
            if (guidanceStaff != null) response.put("guidanceStaffId", guidanceStaff.getId());
        }
        if (STUDENT_ROLE.name().equals(user.getRole())) {
            Student student = studentRepository.findByUser(user);
            if (student != null) response.put("studentId", student.getId());
        }

        return response;
    }

    private String encodePassword(String password) {
        return bCryptPasswordEncoder.encode(password);
    }

    private String generateUserId() {
        return RandomStringUtils.randomNumeric(10);
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 6)
            throw new InvalidPasswordException("Password must be at least 6 characters");
    }

    private String determineOrganization(String sectionName) {
        if (sectionName == null) return "ROCS";
        String prefix = sectionName.split("[-–]")[0].toUpperCase();
        switch (prefix) {
            case "ECE", "BSECE" -> { return "ECE"; }
            case "HM", "BSHM" -> { return "HM"; }
            case "TM", "BSTM" -> { return "TM"; }
            case "BSA" -> { return "BSA"; }
            default -> { return "ROCS"; }
        }
    }

    private String determineClusterName(String organization, String sectionName) {
        if (organization == null) return "CETE";
        return organization.equals("ROCS") ? "CETE" : organization;
    }

    private String determineCourse(String sectionName) {
        if (sectionName == null) return "UNKNOWN";
        String prefix = sectionName.split("[-–]")[0].toUpperCase();
        return switch (prefix) {
            case "IT" -> "BSIT";
            case "CS" -> "BSCS";
            case "ECE" -> "BSECE";
            case "HM" -> "BSHM";
            case "TM" -> "BSTM";
            case "BSA" -> "BSA";
            default -> "BS" + prefix;
        };
    }
}