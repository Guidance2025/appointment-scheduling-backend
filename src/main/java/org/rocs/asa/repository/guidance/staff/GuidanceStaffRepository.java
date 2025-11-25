package org.rocs.asa.repository.guidance.staff;

import org.rocs.asa.domain.guidance.staff.GuidanceStaff;
import org.rocs.asa.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GuidanceStaffRepository extends JpaRepository<GuidanceStaff,Long> {
    GuidanceStaff findByUser(User user);
}
