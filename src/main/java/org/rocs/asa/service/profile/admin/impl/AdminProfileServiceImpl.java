package org.rocs.asa.service.profile.admin.impl;


import org.rocs.asa.domain.account.profile.request.AdminProfileDto;
import org.rocs.asa.domain.user.User;
import org.rocs.asa.exception.domain.UserNotFoundException;
import org.rocs.asa.repository.user.UserRepository;
import org.rocs.asa.service.profile.admin.AdminProfileService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminProfileServiceImpl implements AdminProfileService {
    private static Logger LOGGER = LoggerFactory.getLogger(AdminProfileServiceImpl.class);
    private UserRepository userRepository;

    @Autowired
    public AdminProfileServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public AdminProfileDto getAdminProfile(String userId) {
        User userAdmin = userRepository.findByUserId(userId);
        if (userAdmin == null ) {
            LOGGER.error("User Not Found");
            throw new UserNotFoundException("User Not Found ");
        }
        AdminProfileDto dto = new AdminProfileDto();
        dto.setFirstname(userAdmin.getPerson().getFirstName());
        dto.setLastname(userAdmin.getPerson().getLastName());
        dto.setEmail(userAdmin.getPerson().getEmail());
        return dto;
    }
}
