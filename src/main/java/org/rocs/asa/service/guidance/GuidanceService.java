package org.rocs.asa.service.guidance;

import org.rocs.asa.domain.guidance.staff.GuidanceStaff;
import org.rocs.asa.domain.user.User;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface GuidanceService {
   GuidanceStaff findAuthenticatedGuidanceStaff();
   GuidanceStaff findByUser(User user);

   List<GuidanceStaff> findActiveGuidanceStaff();
}
