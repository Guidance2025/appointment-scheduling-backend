package org.rocs.asa.utils.security.constant.authority;

public class Authorities {
    /**
     * authorities for general users
     * */
    public static String[] USER_AUTHORITIES = {"user:read"};
    /**
     * Authorities for Guidance
     * */
    public static String[] GUIDANCE_AUTHORITIES = {"user:read","user:update","user:create"};
    /**
     * Authorities for teacher
     * */
    public static String[] STUDENT_AUTHORITIES = {"user:read","user:update","user:create"};
    /**
     * Authorities for admin
     * */
    public static String[] ADMIN_AUTHORITIES = {"user:read","user:create","user:update","user:delete"};
}
