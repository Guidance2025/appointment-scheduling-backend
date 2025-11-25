package org.rocs.asa.utils.admin;

import org.apache.commons.lang3.RandomStringUtils;
import org.rocs.asa.domain.person.Person;
import org.rocs.asa.domain.user.User;
import org.rocs.asa.repository.person.PersonRepository;
import org.rocs.asa.repository.user.UserRepository;
import org.rocs.asa.utils.security.enumeration.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Date;

/**
 * Automatically creates default admin account on application startup
 * Only runs if no admin account exists
 */
@Component
public class AdminAccountInitializer implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminAccountInitializer.class);

    private final UserRepository userRepository;
    private final PersonRepository personRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    @Autowired
    public AdminAccountInitializer(UserRepository userRepository,
                                   PersonRepository personRepository,
                                   BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.personRepository = personRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Value("${admin.default.username:admin}")
    private String defaultAdminUsername;

    @Value("${admin.default.password:123123}")
    private String defaultAdminPassword;

    @Value("${admin.default.email:admin@guidance.edu}")
    private String defaultAdminEmail;

    @Value("${admin.auto-create:true}")
    private boolean autoCreateAdmin;

    @Override
    public void run(String... args) {
        if (!autoCreateAdmin) {
            LOGGER.info("Admin auto-creation is disabled. Skipping...");
            return;
        }

        if (userRepository.findByRole(Role.ADMIN_ROLE.name()) != null) {
            LOGGER.info("Admin account already exists. Skipping creation.");
            return;
        }


        if (userRepository.findUserByUsername(defaultAdminUsername) != null) {
            LOGGER.warn("Username '{}' already exists but is not an admin. Skipping creation.", defaultAdminUsername);
            return;
        }
        if (userRepository.findUserByPersonEmail(defaultAdminEmail) != null) {
            LOGGER.warn("Email '{}' already exists. Skipping admin creation.", defaultAdminEmail);
            return;
        }

        try {

            Person adminPerson = new Person();
            adminPerson.setFirstName("System");
            adminPerson.setMiddleName("Default");
            adminPerson.setLastName("Administrator");
            adminPerson.setAge(30);
            adminPerson.setGender("Male");
            adminPerson.setEmail(defaultAdminEmail);
            adminPerson.setAddress("System");
            adminPerson.setContactNumber("09999999999");

            User adminUser = new User();
            adminUser.setPerson(adminPerson);
            adminUser.setUserId(generateUserId());
            adminUser.setUsername(defaultAdminUsername);
            adminUser.setPassword(passwordEncoder.encode(defaultAdminPassword));
            adminUser.setActive(true);
            adminUser.setLocked(false);
            adminUser.setJoinDate(new Date());
            adminUser.setRole(Role.ADMIN_ROLE.name());
            adminUser.setAuthorities(Arrays.asList(Role.ADMIN_ROLE.getAuthorities()));

            userRepository.save(adminUser);

            LOGGER.info("═══════════════════════════════════════════════════════");
            LOGGER.info("  DEFAULT ADMIN ACCOUNT CREATED SUCCESSFULLY");
            LOGGER.info("  Username: {}", defaultAdminUsername);
            LOGGER.info("  Password: {}", defaultAdminPassword);
            LOGGER.info("  Email: {}", defaultAdminEmail);
            LOGGER.warn("   SECURITY WARNING: Change password immediately!");
            LOGGER.info("═══════════════════════════════════════════════════════");

        } catch (Exception e) {
            LOGGER.error("Failed to create default admin account: {}", e.getMessage(), e);
        }
    }

    private String generateUserId() {
        return RandomStringUtils.randomNumeric(10);
    }
}
