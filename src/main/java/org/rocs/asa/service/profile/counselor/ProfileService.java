package org.rocs.asa.service.profile.counselor;

import org.rocs.asa.domain.account.profile.request.CounselorProfileDto;
import org.rocs.asa.domain.guidance.staff.GuidanceStaff;
import org.rocs.asa.domain.guidance.staff.request.profile.UpdateGuidanceStaffProfileRequest;
import org.rocs.asa.domain.person.Person;

public interface ProfileService {
    CounselorProfileDto getCounselorProfile (Long employeeNumber);
    Person updateProfile (String userId , UpdateGuidanceStaffProfileRequest request);
}
