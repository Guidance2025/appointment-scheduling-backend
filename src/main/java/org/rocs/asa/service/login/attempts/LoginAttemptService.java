package org.rocs.asa.service.login.attempts;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
@Component
public class LoginAttemptService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginAttemptService.class);
    private static final int MAX_NUMBER_OF_ATTEMPTS = 5;
    private static final int ATTEMPTS_INCREMENT = 1;
    private LoadingCache<String, Integer> loginAttemptCache;

    /**
     * initialize the loginAttemptsService that holds 100 entries and expires within 15 minutes
     * */
    public LoginAttemptService() {
        super();
        loginAttemptCache = CacheBuilder.newBuilder()
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .maximumSize(100)
                .build(new CacheLoader<String, Integer>() {
                    @Override
                    public Integer load(String key) throws Exception {
                        return 0;
                    }
                });
    }
    /**
     * this adds the user to login attempts and increments the login attempts of the user
     * @param username username to add to login attempt cache
     * */
    public void addUserToLoginAttemptCache(String username){
        int loginAttempts = 0;
        try {
            loginAttempts = loginAttemptCache.get(username) + ATTEMPTS_INCREMENT;
        } catch (ExecutionException e) {
            LOGGER.error("Execution Exception occurred{}", e);
        }
        loginAttemptCache.put(username,loginAttempts);
    }

    /**
     * removes the user from login attempt cache
     * @param username username to remove from login attempt cache
     */
    public void evictUserToLoginAttemptCache(String username){
        loginAttemptCache.invalidate(username);
    }
    /**
     * checks if the login attempts of the user exceeds the maximum attempts allowed
     *
     * @param username username to check if the max attempts exceeds
     * @return true if the user has reached or exceeded the maximum allowed login attempts
     * */
    public boolean hasExceedMaxAttempts(String username){
        try {
            return loginAttemptCache.get(username) > MAX_NUMBER_OF_ATTEMPTS;
        } catch (ExecutionException e) {
            LOGGER.error("Execution Exception occurred{}", e);
        }
        return false;
    }
}
