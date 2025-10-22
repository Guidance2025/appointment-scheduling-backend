package org.rocs.asa.controller.user;

import jakarta.mail.MessagingException;
import org.rocs.asa.domain.registration.Registration;
import org.rocs.asa.domain.user.User;
import org.rocs.asa.domain.user.principal.UserPrincipal;
import org.rocs.asa.exception.domain.UserNotFoundException;
import org.rocs.asa.service.notification.NotificationService;
import org.rocs.asa.service.user.UserService;
import org.rocs.asa.utils.security.jwt.token.provider.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static org.rocs.asa.utils.security.constant.SecurityConstant.JWT_TOKEN_HEADER;

/**
 * The {@code UserController} class use to implement the registration and login functionality of Infirmary web application
 * */
@RestController
@RequestMapping("/user")
@CrossOrigin("http://localhost:5173")
public class UserController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private  NotificationService notificationService;

    /**
     * Constructs a new {@code UserController} with the required dependencies.
     *
     * This constructor is annotated with {@code Autowired} allows
     * Spring to inject the necessary beans at runtime.
     *
     * @param userService the service layer for managing user operations
     * @param authenticationManager the Spring Security authentication manager used to authenticate user credentials
     * @param jwtTokenProvider the provider utility for generating and validating JWT used in secure authentication
     */
    @Autowired
    public UserController(UserService userService, AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider, NotificationService notificationService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.notificationService = notificationService;
    }

    /**
     * {@code login} used to handle the login request, this authenticates a user login based on the provided credential
     * @param user is the object containing the user's credential from the { @code RequestBody}.
     *
     * @return ResponseEntity containing the message, JWT Header and the Http Status
     * */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody User user){
        authUserLogin(user.getUsername(), user.getPassword());
        User loginUser = this.userService.findUserByUsername(user.getUsername());
        UserPrincipal userPrincipal = new UserPrincipal(loginUser);
        HttpHeaders jwtHeader = provideJwtHeader(userPrincipal);
        Map<String,Object> response = userService.buildLoginResponse(loginUser);
        return new ResponseEntity<>(response,jwtHeader, HttpStatus.OK);
    }
    /**
     * {@code forgetPassword} used to handle the forget password request, this accepts the object
     * @param user that contains the credential provided by the user
     * @return ResponseEntity containing the user object, and  Http Status
     * */
    @PostMapping("/forget-password")
    public ResponseEntity<User> forgetPassword(@RequestBody User user) throws MessagingException {
            User newPassword =  userService.forgetPassword(user);
            return ResponseEntity.ok(newPassword);
    }

    private void authUserLogin(String username, String password){
        this.authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username,password));
    }
    private HttpHeaders provideJwtHeader(UserPrincipal userPrincipal){
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(JWT_TOKEN_HEADER,this.jwtTokenProvider.generateJwtToken(userPrincipal));
        return httpHeaders;
    }
}
