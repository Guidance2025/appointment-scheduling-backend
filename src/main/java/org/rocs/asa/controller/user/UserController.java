package org.rocs.asa.controller.user;

import jakarta.mail.MessagingException;
import org.rocs.asa.domain.http.response.HttpResponse;
import org.rocs.asa.domain.password.reset.PasswordResetRequest;
import org.rocs.asa.domain.user.User;
import org.rocs.asa.domain.user.principal.UserPrincipal;
import org.rocs.asa.exception.domain.InvalidTokenException;
import org.rocs.asa.service.login.attempts.LoginAttemptService;
import org.rocs.asa.service.notification.NotificationService;
import org.rocs.asa.service.password.reset.PasswordResetTokenService;
import org.rocs.asa.service.user.UserService;
import org.rocs.asa.utils.security.jwt.token.provider.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static org.rocs.asa.utils.security.constant.SecurityConstant.JWT_TOKEN_HEADER;

/**
 * The {@code UserController} class use to implement the registration and login functionality of Infirmary web application
 * */
@RestController
@RequestMapping("/user")
@CrossOrigin("*")
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private NotificationService notificationService;
    private PasswordResetTokenService passwordResetService;
    private LoginAttemptService loginAttemptService;

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
    public UserController(UserService userService,
                          AuthenticationManager authenticationManager,
                          JwtTokenProvider jwtTokenProvider,
                          NotificationService notificationService,
                          PasswordResetTokenService passwordResetService,
                          LoginAttemptService loginAttemptService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.notificationService = notificationService;
        this.passwordResetService = passwordResetService;
        this.loginAttemptService = loginAttemptService;
    }

    /**
     * {@code login} used to handle the login request, this authenticates a user login based on the provided credential
     * @param user is the object containing the user's credential from the { @code RequestBody}.
     *
     * @return ResponseEntity containing the message, JWT Header and the Http Status
     * */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        try {

            authUserLogin(user.getUsername(), user.getPassword());
            User loginUser = this.userService.findUserByUsername(user.getUsername());
            UserPrincipal userPrincipal = new UserPrincipal(loginUser);
            HttpHeaders jwtHeader = provideJwtHeader(userPrincipal);

            loginAttemptService.evictUserToLoginAttemptCache(user.getUsername());

            Map<String, Object> response = userService.buildLoginResponse(loginUser);
            LOGGER.info("User logged in successfully: {}", user.getUsername());
            return new ResponseEntity<>(response, jwtHeader, HttpStatus.OK);

        } catch (LockedException e) {
            LOGGER.warn("🔒 Login attempt for locked account: {}", user.getUsername());

            boolean isFailedAttemptLock = loginAttemptService.hasExceedMaxAttempts(user.getUsername());

            if (isFailedAttemptLock) {
                return createStructuredErrorResponse(
                        HttpStatus.LOCKED,
                        "ACCOUNT LOCKED DUE TO MULTIPLE FAILED LOGIN ATTEMPTS",
                        "FAILED_ATTEMPTS",
                        180
                );
            } else {
                // FIXED: Return structured error response for admin lock
                return createStructuredErrorResponse(
                        HttpStatus.LOCKED,
                        "ACCOUNT HAS BEEN LOCKED BY ADMINISTRATOR",
                        "ADMIN_LOCK",
                        null // No duration for permanent locks
                );
            }

        } catch (DisabledException e) {
            LOGGER.warn(" Login attempt for disabled account: {}", user.getUsername());
            return createStructuredErrorResponse(
                    HttpStatus.FORBIDDEN,
                    "YOUR ACCOUNT HAS BEEN DISABLED",
                    "DISABLED",
                    null
            );

        } catch (BadCredentialsException e) {
            LOGGER.warn(" Invalid credentials for username: {}", user.getUsername());
            return createHttpResponse(HttpStatus.UNAUTHORIZED, "USERNAME/PASSWORD IS INCORRECT");

        } catch (InternalAuthenticationServiceException e) {
            LOGGER.error("⚠Internal authentication error for username {}: ", user.getUsername(), e);

            if (e.getCause() instanceof LockedException) {
                LOGGER.warn(" Account locked (wrapped exception): {}", user.getUsername());

                boolean isFailedAttemptLock = loginAttemptService.hasExceedMaxAttempts(user.getUsername());

                if (isFailedAttemptLock) {
                    return createStructuredErrorResponse(
                            HttpStatus.LOCKED,
                            "ACCOUNT LOCKED DUE TO MULTIPLE FAILED LOGIN ATTEMPTS",
                            "FAILED_ATTEMPTS",
                            180
                    );
                } else {
                    return createStructuredErrorResponse(
                            HttpStatus.LOCKED,
                            "ACCOUNT HAS BEEN LOCKED BY ADMINISTRATOR",
                            "ADMIN_LOCK",
                            null
                    );
                }
            }
            else if (e.getCause() instanceof DisabledException) {
                LOGGER.warn(" Account disabled (wrapped exception): {}", user.getUsername());
                return createStructuredErrorResponse(
                        HttpStatus.FORBIDDEN,
                        "YOUR ACCOUNT HAS BEEN DISABLED",
                        "DISABLED",
                        null
                );
            }
            else {
                return createHttpResponse(HttpStatus.UNAUTHORIZED, "USERNAME/PASSWORD IS INCORRECT");
            }
        } catch (Exception e) {
            LOGGER.error(" Login error for username {}: ", user.getUsername(), e);
            return createHttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, "LOGIN FAILED");
        }
    }

    /**
     * Initiates password reset process
     * Sends reset link to user's email
     *
     * @param request containing username and new password
     * @return success message
     */
    @PostMapping("/password-reset/initiate")
    public ResponseEntity<Map<String, String>> initiatePasswordReset(@RequestBody PasswordResetRequest request) throws MessagingException {
        this.userService.initiatePasswordReset(request.getUsername(), request.getNewPassword());
        Map<String, String> response = Map.of(
                "message", "Password reset email sent successfully. Please check your inbox."
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Verifies token and completes password reset
     * This endpoint is called when user clicks the email link
     *
     * @param token from email link
     * @return success message and redirects to login
     */
    @GetMapping("/password-reset/verify")
    public ResponseEntity<Map<String, String>> verifyPasswordReset(@RequestParam String token) throws InvalidTokenException {
        this.userService.verifyAndCompletePasswordReset(token);
        Map<String, String> response = Map.of("success", "Password successfully changed. You can now login with your new password.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private void authUserLogin(String username, String password) {
        this.authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }

    private HttpHeaders provideJwtHeader(UserPrincipal userPrincipal) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(JWT_TOKEN_HEADER, this.jwtTokenProvider.generateJwtToken(userPrincipal));
        return httpHeaders;
    }

    /**
     * FIXED: Helper method to create consistent HTTP error responses
     */
    private ResponseEntity<HttpResponse> createHttpResponse(HttpStatus status, String message) {
        return new ResponseEntity<>(
                new HttpResponse(
                        status.value(),
                        status,
                        status.getReasonPhrase().toUpperCase(),
                        message.toUpperCase()
                ),
                status
        );
    }

    /**
     *  create structured error responses with lock metadata
     * This provides the frontend with reliable lock type information
     *
     * @param status HTTP status code
     * @param message Error message
     * @param lockType Type of lock (ADMIN_LOCK, FAILED_ATTEMPTS, DISABLED)
     * @param lockDuration Duration in seconds (null for permanent locks)
     * @return ResponseEntity with structured error data
     */
    private ResponseEntity<Map<String, Object>> createStructuredErrorResponse(
            HttpStatus status,
            String message,
            String lockType,
            Integer lockDuration) {

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase().toUpperCase());
        errorResponse.put("message", message.toUpperCase());
        errorResponse.put("lockType", lockType);

        if (lockDuration != null) {
            errorResponse.put("lockDuration", lockDuration);
        }

        errorResponse.put("timestamp", System.currentTimeMillis());

        return new ResponseEntity<>(errorResponse, status);
    }
}