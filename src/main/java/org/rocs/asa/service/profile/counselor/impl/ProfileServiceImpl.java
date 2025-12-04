package org.rocs.asa.service.profile.counselor.impl;

import com.google.api.gax.rpc.AlreadyExistsException;
import org.rocs.asa.domain.account.profile.request.CounselorProfileDto;
import org.rocs.asa.domain.guidance.staff.GuidanceStaff;
import org.rocs.asa.domain.guidance.staff.request.profile.UpdateGuidanceStaffProfileRequest;
import org.rocs.asa.domain.person.Person;
import org.rocs.asa.domain.user.User;
import org.rocs.asa.exception.domain.AlreadyExistException;
import org.rocs.asa.exception.domain.EmailAlreadyExistException;
import org.rocs.asa.exception.domain.EmployeeDoesNotExist;
import org.rocs.asa.exception.domain.GuidanceStaffNotFoundException;
import org.rocs.asa.repository.guidance.staff.GuidanceStaffRepository;
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

    @Autowired
    public ProfileServiceImpl(GuidanceStaffRepository guidanceStaffRepository, UserService userService , UserRepository userRepository) {
        this.guidanceStaffRepository = guidanceStaffRepository;
        this.userService = userService;
        this.userRepository = userRepository;
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
    public GuidanceStaff updateProfile(Long guidanceStaffId, UpdateGuidanceStaffProfileRequest request) {

        GuidanceStaff guidanceStaff = guidanceStaffRepository.findById(guidanceStaffId)
                .orElseThrow(() -> new GuidanceStaffNotFoundException("Guidance Staff not found"));

        Person person = guidanceStaff.getPerson();

        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (!request.getEmail().equals(person.getEmail())) {
                User existing = userService.findUserByPersonEmail(request.getEmail());
                if (existing != null) {
                    throw new EmailAlreadyExistException("Email Already Exist");
                }
                person.setEmail(request.getEmail());
            }
        }
        if(request.getContactNumber() != null && !request.getContactNumber().trim().isEmpty())
            if(request.getContactNumber().equals(person.getContactNumber())) {
                User existing = userRepository.findUserByPersonContactNumber(request.getContactNumber());
                if(existing != null ) {
                    throw new AlreadyExistException("Contact Number Already Exist");
                }
                person.setContactNumber(request.getContactNumber());
            }

        return guidanceStaffRepository.save(guidanceStaff);
    }
}
