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

    public void addUserToLoginAttemptCache(String username){
        int loginAttempts = 0;
        try {
            loginAttempts = loginAttemptCache.get(username) + ATTEMPTS_INCREMENT;
            loginAttemptCache.put(username, loginAttempts);

            LOGGER.warn("⚠ Username: {} | Failed Attempts: {}/{}",
                    username, loginAttempts, MAX_NUMBER_OF_ATTEMPTS);

        } catch (ExecutionException e) {
            LOGGER.error(" Execution Exception occurred: {}", e.getMessage());
        }
    }

    public void evictUserToLoginAttemptCache(String username){
        loginAttemptCache.invalidate(username);
        LOGGER.info(" Login attempts cleared for username: {}", username);
    }

    public boolean hasExceedMaxAttempts(String username){
        try {
            int attempts = loginAttemptCache.get(username);
            boolean exceeded = attempts >= MAX_NUMBER_OF_ATTEMPTS;
            LOGGER.warn(" CHECK ATTEMPTS - Username: {}, Current: {}, Max: {}, Exceeded: {}",
                    username, attempts, MAX_NUMBER_OF_ATTEMPTS, exceeded);
            if (exceeded) {
                LOGGER.error(" MAX LOGIN ATTEMPTS REACHED - Username: {} ({}/{})",
                        username, attempts, MAX_NUMBER_OF_ATTEMPTS);
            }
            return exceeded;

        } catch (ExecutionException e) {
            LOGGER.error("Execution Exception occurred: {}", e.getMessage());
        }
        return false;
    }
}