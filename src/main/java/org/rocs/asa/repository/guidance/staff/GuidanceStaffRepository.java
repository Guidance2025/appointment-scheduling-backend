package org.rocs.asa.repository.guidance.staff;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GuidanceStaffRepository extends JpaRepository<org.rocs.asa.domain.guidance.staff.GuidanceStaff,Long> {

}
