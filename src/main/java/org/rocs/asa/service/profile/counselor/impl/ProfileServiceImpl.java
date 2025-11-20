package org.rocs.asa.service.profile.counselor.impl;

import org.rocs.asa.domain.account.profile.request.CounselorProfileDto;
import org.rocs.asa.domain.guidance.staff.GuidanceStaff;
import org.rocs.asa.exception.domain.EmployeeDoesNotExist;
import org.rocs.asa.repository.guidance.staff.GuidanceStaffRepository;
import org.rocs.asa.service.profile.counselor.ProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProfileServiceImpl implements ProfileService {
    private static Logger LOGGER = LoggerFactory.getLogger(ProfileServiceImpl.class);
    private GuidanceStaffRepository guidanceStaffRepository;

    @Autowired
    public ProfileServiceImpl(GuidanceStaffRepository guidanceStaffRepository) {
        this.guidanceStaffRepository = guidanceStaffRepository;
    }

    @Override
    public CounselorProfileDto getCounselorProfile(Long employeeNumber) {
        GuidanceStaff employee = guidanceStaffRepository.findById(employeeNumber)
                .orElseThrow(() -> new EmployeeDoesNotExist("Employee Does Not Exist") );
        CounselorProfileDto dto = new CounselorProfileDto();
        dto.setFirstName(employee.getPerson().getFirstName());
        dto.setMiddleName(employee.getPerson().getMiddleName());
        dto.setLastName(employee.getPerson().getLastName());
        dto.setEmail(employee.getPerson().getEmail());
        dto.setContactNumber(employee.getPerson().getContactNumber());
        dto.setPositionInRc(employee.getPositionInRc());
        return dto;
    }
}
