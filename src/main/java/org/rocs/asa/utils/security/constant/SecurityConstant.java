package org.rocs.asa.utils.security.constant;
/**
 * {@code SecurityConstant} provides the constants for security configuration
 * */
public class SecurityConstant {
    /**
     * token expiration time in milliseconds
     */
    public static final long EXPIRATION_TIME = 432000000;
    /**
     * token prefix bearer in Http headers
     * */
    public static final String TOKEN_PREFIX = "Bearer ";
    /**
     * Header for Jwt token in Http request
     * */
    public static final String JWT_TOKEN_HEADER = "Jwt-Token";
    /**
     * an error message indicates that the token cannot be verified
     * */
    public static final String TOKEN_CANNOT_BE_VERIFIED = "Token cannot be verified";
    /**
     * application name
     * */
    public static final String GUIDANCE = "Appointment Scheduling";
    /**
     * application description
     * */
    public static final String GUIDANCE_ADMINISTRATION = "Appointment Scheduling Service";
    /**
     * key for storing authorities in JWT claims
     * */
    public static final String AUTHORITIES = "authorities";
    /**
     * a forbidden error message indicates that the user cannot access the page
     * */
    public static final String FORBIDDEN_MESSAGE = "You cannot access this page";
    /**
     * access denied message indicates that the user don't have permission to access the page
     * */
    public static final String ACCESS_DENIED_MESSAGE = "You do not have permission to access this page";
    /**
     * https method for preflight request
     * */
    public static final String OPTIONS_HTTP_METHODS = "OPTIONS";
    /**
     * URL's that are available in public
     * */
    public static final String[] PUBLIC_URLS = {
            "/user/login",
            "/user/register",
            "/notification/**",
            "/user/password-reset/initiate",
            "/user/password-reset/verify",
    };
}