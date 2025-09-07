package org.rocs.asa.listener.authentication.failure;

import org.rocs.asa.service.login.attempts.LoginAttemptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

@Component
public class AuthenthicationFailureListener {

    private LoginAttemptService loginAttemptsService;

    /**
     * initialize the Login Attempts Service
     *
     * @param loginAttemptsService service responsible for handling login attempts
     */
    @Autowired
    public AuthenthicationFailureListener(LoginAttemptService loginAttemptsService) {
        this.loginAttemptsService = loginAttemptsService;
    }

    /**
     * Listens for authentication failures caused by bad credentials and adds the user's
     *
     * @param authenticationFailureBadCredentialsEvent event triggered on authentication failure
     */
    @EventListener
    public void onAuthenticationFailure(AuthenticationFailureBadCredentialsEvent authenticationFailureBadCredentialsEvent) {
        Object principal = authenticationFailureBadCredentialsEvent.getAuthentication().getPrincipal();

        if (principal instanceof String) {
            String username = (String) authenticationFailureBadCredentialsEvent.getAuthentication().getPrincipal();
            loginAttemptsService.addUserToLoginAttemptCache(username);
        }
    }
}