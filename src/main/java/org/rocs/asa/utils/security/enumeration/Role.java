package org.rocs.asa.utils.security.enumeration;

import static org.rocs.asa.utils.security.constant.authority.Authorities.*;

public enum Role {
    /**
     * role for general user with read access
     * */
    USER_ROLE(USER_AUTHORITIES),
    /**
     * role for guidance with create,read and update access
     * */
    GUIDANCE_ROLE(GUIDANCE_AUTHORITIES),

    /**
     * role for student with create,read and update access
     * */
    STUDENT_ROLE(STUDENT_AUTHORITIES),
    /**
     * role for admin with create,read, update, and delete access
     * */
    ADMIN_ROLE(ADMIN_AUTHORITIES);

    private final String[] authorities;

    /**
     * Constructor for the Role enum.
     *
     * @param authorities authorities associated with the role
     */
    Role(String... authorities) {
        this.authorities = authorities;
    }
    /**
     * Get the authorities associated with the role.
     *
     * @return array of authorities
     */
    public String[] getAuthorities(){
        return authorities;
    }
}
