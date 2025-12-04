package org.rocs.asa.service.accounts;

import jakarta.mail.MessagingException;
import org.rocs.asa.domain.account.dto.GuidanceStaffDto;
import org.rocs.asa.domain.account.dto.StudentAccountDto;
import org.rocs.asa.domain.user.User;

import java.util.List;

public interface AccountsService {
    List<User> getAllAccounts();

    List<GuidanceStaffDto> getGuidanceStaffAccount();

    List <StudentAccountDto> getStudentAccount();

    void softDeleteStudentAccount (String studentNumber);

    void softDeleteEmployeeAccount(Long id);

    void updateStudentCredentials(String studentNumber, String newPassword, Boolean isLocked) throws MessagingException;

    void updateGuidanceEmployeeCredentials(Long id , String email, Boolean isLocked);
}
