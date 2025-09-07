package org.rocs.asa.listener.authentication.success;

import org.rocs.asa.service.login.attempts.LoginAttemptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

/**
 * {@code AuthenticationSuccessListener} is a component that handles authentication success events.
 */
@Component
public class AuthenticationSuccessListener {
    private LoginAttemptService loginAttemptsService;

    /**
     * create a constructor for AuthenticationSuccessListener
     *
     * @param loginAttemptsService injects the service that is responsible for managing login attempts
     * */
    @Autowired
    public AuthenticationSuccessListener(LoginAttemptService loginAttemptsService) {
        this.loginAttemptsService = loginAttemptsService;
    }
    /**
     * listen for successfull login attempts
     *
     * @param authenticationSuccessEvent triggers on successful login
     * */
    @EventListener
    public void onAuthenticationSuccessListener(AuthenticationSuccessEvent authenticationSuccessEvent){
        Object principal = authenticationSuccessEvent.getAuthentication().getPrincipal();

        if(principal instanceof String){
            String username = (String) authenticationSuccessEvent.getAuthentication().getPrincipal();
            loginAttemptsService.evictUserToLoginAttemptCache(username);
        }
    }
}
