package org.rocs.asa.domain.email.constant;
/**
 * this is used to hold the constants use for email service
 * */
public class EmailConstant {

    /**
     * This protocol is used for sending email.
     * Simple Mail Transfer Protocol ensure the email communication
     * that is encrypted from the sender to the email server.
     */
    public static final String SIMPLE_MAIL_TRANSFER_PROTOCOL = "smtps";

    /**
     * Email address used as the sender's address.
     */
    public static final String USERNAME = "neonael.angcaya.nazareno@gmail.com";

    /**
     * Password used for authenticating the sender's email account.
     */
    public static final String PASSWORD = "nagmpzoyusupvmtu";

    /**
     * This is the email address that will appear as the "From" address in sent emails.
     */
    public static final String FROM_EMAIL = "neonael.angcaya.nazareno@gmail.com";

    /**
     * This constant will be used as the subject of the email welcoming users.
     */
    public static final String EMAIL_SUBJECT = "Password Reset Verification - GABAY";
    /**
     * This constant will be used as the subject for emails sent to users when a new account is created.
     */
    public static final String EMAIL_STAFF_NEW_REGISTRATION_SUBJECT = "New Account Created - GABAY";

    public static final String EMAIL_STUDENT_NEW_PASSWORD_SUBJECT = "Password Changed - GABAY";
    /**
     * Simple Mail Transfer Protocol server address for Gmail which will handle sending emails.
     */
    public static final String GMAIL_SMTP_SERVER = "smtp.gmail.com";

    /**
     * Simple Mail Transfer Protocol host.
     * This specifies the email server host to use.
     */
    public static final String SMTP_HOST = "mail.smtp.host";

    /**
     * Simple Mail Transfer Protocol authentication.
     * This setting ensures that the SMTP server requires authentication (username and password).
     */
    public static final String SMTP_AUTH = "mail.smtp.auth";

    /**
     * Specifying the Simple Mail Transfer Protocol port.
     * This constant defines the port number to connect to for sending emails
     */
    public static final String SMTP_PORT = "mail.smtp.port";

    /**
     * Default port for Simple Mail Transfer Protocol.
     */
    public static final int DEFAULT_PORT = 465;

    /**
     * Enabling StartTLS in Simple Mail Transfer Protocol.
     * This setting ensures that the email communication will use TLS encryption if the server supports it.
     */
    public static final String SMTP_STARTTLS_ENABLE = "mail.smtp.starttls.enable";

    /**
     * Requiring StartTLS in Simple Mail Transfer Protocol.
     * This setting forces the connection to use TLS encryption to secure the communication.
     */
    public static final String SMTP_STARTTLS_REQUIRED = "mail.smtp.starttls.required";

}

