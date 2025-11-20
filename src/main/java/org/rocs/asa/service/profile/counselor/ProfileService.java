package org.rocs.asa.service.profile.counselor;

import org.rocs.asa.domain.account.profile.request.CounselorProfileDto;

public interface ProfileService {
    CounselorProfileDto getCounselorProfile (Long employeeNumber);
}
