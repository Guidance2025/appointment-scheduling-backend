package org.rocs.asa.controller.admin;

import org.rocs.asa.domain.account.dto.GuidanceStaffDto;
import org.rocs.asa.domain.account.dto.StudentAccountDto;
import org.rocs.asa.domain.account.profile.request.AdminProfileDto;
import org.rocs.asa.domain.student.information.response.StudentInformation;
import org.rocs.asa.domain.guidance.staff.dto.admin.request.UpdateGuidanceStaffRequest;
import org.rocs.asa.domain.registration.Registration;
import org.rocs.asa.domain.student.request.UpdateStudentRequest;
import org.rocs.asa.domain.user.User;
import org.rocs.asa.service.accounts.AccountsService;
import org.rocs.asa.service.profile.admin.AdminProfileService;
import org.rocs.asa.service.student.inforamation.StudentInformationService;
import org.rocs.asa.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * {@code AdminController} handles all administrative operations
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    private UserService userService;
    private AccountsService accountsService;
    private StudentInformationService studentInformationService;
    private AdminProfileService adminProfileService;
    /**
     * Constructs a new {@code AdminController} with the required dependencies.
     *
     * This constructor is annotated with {@code Autowired} allows
     * Spring to inject the necessary beans at runtime.
     *
     *  @param userService the service layer for managing user operations
     *  @param accountsService the service layer for managing account operations
     *  @param adminProfileService the service layer for managing admin operations
     *
     */
    @Autowired
    public AdminController(UserService userService, AccountsService accountsService,
                           StudentInformationService studentInformationService,
                           AdminProfileService adminProfileService) {
        this.userService = userService;
        this.accountsService = accountsService;
        this.studentInformationService = studentInformationService;
        this.adminProfileService = adminProfileService;
    }

    /**
     * {@code register} used to handle the registration request, this accepts the object
     * @param registration that contains the credential provided by the user
     * @return ResponseEntity containing the user object, and Http Status
     */
    @PostMapping("/register")
    public ResponseEntity<Registration> register(@RequestBody Registration registration){
        Registration newUser = this.userService.registerUser(registration);
        return new ResponseEntity<>(newUser, HttpStatus.OK);
    }

    /**
     * {@code findAllAccounts} used to retrieve all user accounts in the system
     * @return ResponseEntity containing list of all user accounts, and Http Status
     */
    @GetMapping("/accounts")
    public ResponseEntity<List<User>> findAllAccounts(){
        List<User> accounts = accountsService.getAllAccounts();
        return new ResponseEntity<>(accounts, HttpStatus.OK);
    }

    /**
     * {@code getGuidanceAccount} used to retrieve all guidance staff accounts
     * @return ResponseEntity containing list of guidance staff accounts, and Http Status
     */
    @GetMapping("/guidance-staff-accounts")
    public ResponseEntity<List<GuidanceStaffDto>> getGuidanceAccount(){
        List<GuidanceStaffDto> guidanceStaffAccount = accountsService.getGuidanceStaffAccount();
        return new ResponseEntity<>(guidanceStaffAccount, HttpStatus.OK);
    }

    /**
     * {@code getAllStudent} used to retrieve all student information from the database
     * @return ResponseEntity containing list of all student information, and Http Status
     */
    @GetMapping("/student-information")
    public ResponseEntity<List<StudentInformation>> getAllStudent(){
        List<StudentInformation> allStudentInformation = studentInformationService.getAllStudent();
        return new ResponseEntity<>(allStudentInformation, HttpStatus.OK);
    }

    /**
     * {@code getAdminProfile} used to retrieve admin profile information by user ID
     * @param userId that identifies the admin user
     * @return ResponseEntity containing admin profile details, and Http Status
     */
    @GetMapping("/profile/{userId}")
    public ResponseEntity<AdminProfileDto> getAdminProfile(@PathVariable String userId) {
        AdminProfileDto adminProfileDto = adminProfileService.getAdminProfile(userId);
        return new ResponseEntity<>(adminProfileDto, HttpStatus.OK);
    }

    /**
     * {@code getStudentAccount} used to retrieve all student accounts from the system
     * @return ResponseEntity containing list of student accounts, and Http Status
     */
    @GetMapping("/student-accounts")
    public ResponseEntity<List<StudentAccountDto>> getStudentAccount() {
        List<StudentAccountDto> studentAccount = accountsService.getStudentAccount();
        return new ResponseEntity<>(studentAccount, HttpStatus.OK);
    }

    /**
     * {@code deleteStudentAccount} used to soft delete a student account by student number
     * @param studentNumber that identifies the student to be deleted
     * @return ResponseEntity containing success message, and Http Status
     */
    @PatchMapping("/delete-student/{studentNumber}")
    public ResponseEntity<String> deleteStudentAccount(@PathVariable String studentNumber) {
        accountsService.softDeleteStudentAccount(studentNumber);
        return new ResponseEntity<>("Student Account Deleted Successfully", HttpStatus.OK);
    }

    /**
     * {@code deleteEmployeeAccount} used to soft delete an employee account by ID
     * @param id that identifies the employee to be deleted
     * @return ResponseEntity containing success message, and Http Status
     */
    @PatchMapping("/delete-employee/{id}")
    public ResponseEntity<String> deleteEmployeeAccount(@PathVariable Long id) {
        accountsService.softDeleteEmployeeAccount(id);
        return new ResponseEntity<>("Employee Account Deleted Successfully", HttpStatus.OK);
    }

    /**
     * {@code updateStudent} used to update student credentials including password and lock status
     * @param request Data Transfer Object the contains student credentials to update
     * @return ResponseEntity containing success message, and Http Status
     */
    @PutMapping("/students/update")
    public ResponseEntity<String> updateStudent(@RequestBody UpdateStudentRequest request) {
        accountsService.updateStudentCredentials(request.getStudentNumber(), request.getNewPassword(),request.getIsLocked());
        return new ResponseEntity<>("Student Credential Successfully Updated", HttpStatus.OK);
    }
    /**
     * {@code updateGuidanceStaff} used to update student credentials including password and lock status
     * @param request Data Transfer Object the contains guidance staff credentials to update
     * @return ResponseEntity containing success message, and Http Status
     */
    @PutMapping("/guidance-staff/update")
    public ResponseEntity<String> updateGuidanceStaff(@RequestBody UpdateGuidanceStaffRequest request) {
        accountsService.updateGuidanceEmployeeCredentials(request.getId(),request.getEmail(),request.getIsLocked());
        return new ResponseEntity<>("Guidance Staff Credentials Successfully Updated",HttpStatus.OK);
    }
}