package org.rocs.asa.service.guidance.impl;

import org.rocs.asa.domain.guidance.staff.GuidanceStaff;
import org.rocs.asa.domain.user.User;
import org.rocs.asa.exception.domain.GuidanceStaffNotFoundException;
import org.rocs.asa.repository.guidance.staff.GuidanceStaffRepository;
import org.rocs.asa.service.guidance.GuidanceService;
import org.rocs.asa.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GuidanceServiceImpl implements GuidanceService  {
    private static Logger LOGGER = LoggerFactory.getLogger(GuidanceService.class);
    private GuidanceStaffRepository guidanceStaffRepository;
    private UserService userService;
    @Autowired
    public GuidanceServiceImpl(GuidanceStaffRepository guidanceStaffRepository, UserService userService) {
        this.guidanceStaffRepository = guidanceStaffRepository;
        this.userService = userService;
    }

    public GuidanceStaff findAuthenticatedGuidanceStaff() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        User loggedInUser = userService.findUserByUsername(username);
            if(loggedInUser == null || loggedInUser.isLocked()) {
                LOGGER.info("Guidance Staff Not Yet Registered ");
                throw new GuidanceStaffNotFoundException("Guidance Staff Not Found");
            }
            GuidanceStaff guidanceStaff = findByUser(loggedInUser);
            if (guidanceStaff == null){
                throw new GuidanceStaffNotFoundException("Guidance Staff Required");
            }

        return guidanceStaff;
    }

    @Override
    public GuidanceStaff findByUser(User user) {
        return guidanceStaffRepository.findByUser(user);
    }

    @Override
    public List<GuidanceStaff> findActiveGuidanceStaff() {
        return guidanceStaffRepository.findAll().stream()
                .filter(staff -> staff.getUser() != null && staff.getUser().isActive())
                .collect(Collectors.toList());
    }

}
