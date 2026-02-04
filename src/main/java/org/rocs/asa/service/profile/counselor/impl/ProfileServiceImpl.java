package org.rocs.asa.service.profile.counselor.impl;

import com.google.api.gax.rpc.AlreadyExistsException;
import org.rocs.asa.domain.account.profile.request.CounselorProfileDto;
import org.rocs.asa.domain.guidance.staff.GuidanceStaff;
import org.rocs.asa.domain.guidance.staff.request.profile.UpdateGuidanceStaffProfileRequest;
import org.rocs.asa.domain.person.Person;
import org.rocs.asa.domain.user.User;
import org.rocs.asa.exception.domain.*;
import org.rocs.asa.repository.guidance.staff.GuidanceStaffRepository;
import org.rocs.asa.repository.person.PersonRepository;
import org.rocs.asa.repository.user.UserRepository;
import org.rocs.asa.service.profile.counselor.ProfileService;
import org.rocs.asa.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProfileServiceImpl implements ProfileService {
    private static Logger LOGGER = LoggerFactory.getLogger(ProfileServiceImpl.class);
    private GuidanceStaffRepository guidanceStaffRepository;
    private UserService userService;
    private UserRepository userRepository;
    private PersonRepository personRepository;

    @Autowired
    public ProfileServiceImpl(GuidanceStaffRepository guidanceStaffRepository, UserService userService , UserRepository userRepository, PersonRepository personRepository) {
        this.guidanceStaffRepository = guidanceStaffRepository;
        this.userService = userService;
        this.userRepository = userRepository;
        this.personRepository = personRepository;
    }

    @Override
    public CounselorProfileDto getCounselorProfile(Long employeeNumber) {
        GuidanceStaff employee = guidanceStaffRepository.findById(employeeNumber)
                .orElseThrow(() -> new EmployeeDoesNotExist("Employee Does Not Exist"));
        CounselorProfileDto dto = new CounselorProfileDto();
        dto.setFirstName(employee.getPerson().getFirstName());
        dto.setMiddleName(employee.getPerson().getMiddleName());
        dto.setLastName(employee.getPerson().getLastName());
        dto.setEmail(employee.getPerson().getEmail());
        dto.setContactNumber(employee.getPerson().getContactNumber());
        dto.setPositionInRc(employee.getPositionInRc());
        return dto;
    }

    @Override
    public Person updateProfile(String userId, UpdateGuidanceStaffProfileRequest request) {
        User user = userRepository.findByUserId(userId);
        if(user == null) {
            LOGGER.info("Failed to attempt update. User does not exist!");
            throw new UserNotFoundException("User does not exist");
        }

        Person validatePersonInformation = user.getPerson();
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (!request.getEmail().equals(validatePersonInformation.getEmail())) {
                User existing = userService.findUserByPersonEmail(request.getEmail());
                if (existing != null) {
                    throw new EmailAlreadyExistException("Email Already Exist");
                }
                validatePersonInformation.setEmail(request.getEmail());
            }
        }

        if (request.getContactNumber() != null && !request.getContactNumber().trim().isEmpty()) {
            if (!request.getContactNumber().equals(validatePersonInformation.getContactNumber())) {
                validatePersonInformation.setContactNumber(request.getContactNumber());
            }
        }

        return personRepository.save(validatePersonInformation);
    }
}
