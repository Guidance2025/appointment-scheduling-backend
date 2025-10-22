package org.rocs.asa.service.accounts.impl;

import org.rocs.asa.domain.user.User;
import org.rocs.asa.repository.user.UserRepository;
import org.rocs.asa.service.accounts.AccountsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class AccountsServiceImpl implements AccountsService {
    private UserRepository userRepository;

    @Autowired
    public AccountsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<User> getAllAccounts() {
        return userRepository.findAll();
    }

    @Override
    public boolean deleteAccountById(Long id) {
        return false;
    }

}