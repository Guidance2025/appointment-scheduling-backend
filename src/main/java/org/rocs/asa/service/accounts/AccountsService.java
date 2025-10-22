package org.rocs.asa.service.accounts;

import org.rocs.asa.domain.user.User;

import java.util.List;

public interface AccountsService {
    List<User> getAllAccounts();



    boolean deleteAccountById (Long id);
}
