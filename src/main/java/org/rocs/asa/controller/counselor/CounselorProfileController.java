package org.rocs.asa.controller.counselor;

import org.rocs.asa.domain.account.profile.request.CounselorProfileDto;
import org.rocs.asa.domain.guidance.staff.GuidanceStaff;
import org.rocs.asa.service.profile.counselor.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/counselor")
@CrossOrigin("*")
public class CounselorProfileController {
    private ProfileService profileService;
    /**
     * Constructs a new {@code CounselorProfileController} with the required dependencies.
     *
     * This constructor is annotated with {@code Autowired} allows
     * Spring to inject the necessary beans at runtime.
     *
     * @param profileService the service layer for managing profile operations
     */
    @Autowired
    public CounselorProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }
    @GetMapping("/profile/{employeeNumber}")
    public ResponseEntity<CounselorProfileDto> getCounselorProfile (@PathVariable Long employeeNumber){
        CounselorProfileDto profile = profileService.getCounselorProfile(employeeNumber);
        return new ResponseEntity<>(profile, HttpStatus.OK);
    }


}
