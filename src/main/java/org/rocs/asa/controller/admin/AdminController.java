package org.rocs.asa.controller.admin;

import org.rocs.asa.domain.dto.student.information.StudentInformation;
import org.rocs.asa.domain.registration.Registration;
import org.rocs.asa.domain.user.User;
import org.rocs.asa.service.accounts.AccountsService;
import org.rocs.asa.service.student.inforamation.StudentInformationService;
import org.rocs.asa.service.student.profile.StudentProfileService;
import org.rocs.asa.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/admin")
public class AdminController {

    private UserService userService;
    private AccountsService accountsService;
    private StudentInformationService studentInformationService;

    @Autowired
    public AdminController(UserService userService,
                           AccountsService accountsService,
                           StudentInformationService studentInformationService) {
        this.userService = userService;
        this.accountsService = accountsService;
        this.studentInformationService = studentInformationService;
    }

    /**
     * {@code register} used to handle the registration request, this accepts the object
     * @param registration that contains the credential provided by the user
     * @return ResponseEntity containing the user object, and  Http Status
     * */
    @PostMapping("/register")
    public ResponseEntity<Registration> register(@RequestBody Registration registration){
        Registration newUser = this.userService.registerUser(registration);
        return new ResponseEntity<>(newUser, HttpStatus.OK);
    }
    @GetMapping("/accounts")
    public ResponseEntity <List<User>> findAllAccounts(){
        List<User> accounts = accountsService.getAllAccounts();
        return new ResponseEntity<>(accounts, HttpStatus.OK);
    }

    @GetMapping("/student-information")
    public ResponseEntity<List<StudentInformation>> findAllStudent (){
        List<StudentInformation> allStudentInformation = studentInformationService.getAllStudent();
        return new ResponseEntity<>(allStudentInformation,HttpStatus.OK);
    }
}
