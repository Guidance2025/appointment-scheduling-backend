package org.rocs.asa.service.profile.admin;

import org.rocs.asa.domain.account.profile.request.AdminProfileDto;

public interface AdminProfileService  {
    AdminProfileDto getAdminProfile (String userId);
}
