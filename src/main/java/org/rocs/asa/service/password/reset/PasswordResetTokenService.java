package org.rocs.asa.service.password.reset;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.rocs.asa.exception.domain.EmailNotFoundException;
import org.rocs.asa.exception.domain.InvalidTokenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
/**
 * Service responsible for handling password reset tokens.
 * <p>
 * This service generates secure tokens for password reset, stores
 * the associated email and encrypted password in a cache, validates
 * tokens, evicts tokens after use, and manages rate limiting attempts.
 * </p>
 */
@Component
public class PasswordResetTokenService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordResetTokenService.class);


    private final Cache<String, Map<String,String>> tokenCache;
    private final Cache<String, Integer> passwordResetAttemptsCache;
    private static final int MAX_ATTEMPTS = 3;


    @Value("${password.reset.token.expiration.hours:24}")
    private int tokenExpirationHours;

    private static final SecureRandom random = new SecureRandom();

    @Autowired
    public PasswordResetTokenService(){
        super();

        this.tokenCache = CacheBuilder.newBuilder()
                .expireAfterWrite(24, TimeUnit.HOURS)
                .maximumSize(1000)
                .build();

        this.passwordResetAttemptsCache = CacheBuilder.newBuilder()
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .maximumSize(500)
                .build();
    }


    /**
     * Generates a unique, secure token for password reset and stores
     * the associated user's email and encrypted password in the cache.
     *
     * @param email             the email of the user requesting password reset
     * @param encryptedPassword the encrypted password to be set after reset
     * @return a unique token string to be sent to the user for verification
     */
    public String generateSecureToken(String email , String encryptedPassword) {
        String token = UUID.randomUUID().toString();
        Map<String,String> tokenData = new HashMap<>();
        tokenData.put("password",encryptedPassword);
        tokenData.put("email",email);
        tokenCache.put(token,tokenData);
        LOGGER.info("Generated password reset token for email: {}", email);
        return token;
    }
    /**
     * Validates a password reset token.
     * <p>
     * If the token exists in the cache, it is considered valid and its
     * associated data (email and password) is returned. Otherwise, an
     * InvalidTokenException is thrown.
     * </p>
     *
     * @param token the token to validate
     * @return a map containing token data: "email" and "password"
     * @throws InvalidTokenException if the token is expired, already used, or invalid
     */
    public Map<String,String> validateToken(String token) throws InvalidTokenException {
        Map<String, String> tokenData = tokenCache.getIfPresent(token);
        if (tokenData == null) {
            throw new InvalidTokenException("Token is already Expired or Used");
        }
        return tokenData;
    }

    /**
     * Removes a token from the cache after successful usage.
     *
     * @param token the token to be invalidated
     */
    public void evictTokenInCache (String token) {
        tokenCache.invalidate(token);
        LOGGER.info("Token removed in cache : {}",token);
    }
    /**
     * Clears the password reset attempt count for a specific email.
     *
     * @param email the user's email for which attempts are cleared
     */
    public void clearAttempts(String email){
        passwordResetAttemptsCache.invalidate(email);
        LOGGER.info("Cleared Passwords Attempts for Email {} ", email);
    }
    /**
     * Increments the password reset attempt count for a specific email.
     * <p>
     * This is used to track failed password reset attempts and enforce
     * rate limiting.
     * </p>
     *
     * @param email the user's email for which the attempt is incremented
     */
    public void incrementAttempts (String email) {
        Integer attempt = passwordResetAttemptsCache.getIfPresent(email);
        int newAttempt = attempt == null ? 1 : attempt + 1;
        LOGGER.info("Attempt :  {}", newAttempt);
        passwordResetAttemptsCache.put(email,newAttempt);
    }
    /**
     * Checks whether a user has exceeded the maximum allowed password reset attempts.
     *
     * @param email the user's email to check
     * @return true if the number of attempts for this email is greater than or equal to MAX_ATTEMPTS, false otherwise
     */
    public boolean exceedMaxAttempts(String email) {
        if(email == null) {
            throw new EmailNotFoundException("Email not  found");
        }
        Integer attempts  = passwordResetAttemptsCache.getIfPresent(email);
       return attempts != null && attempts >= MAX_ATTEMPTS;
    }
}