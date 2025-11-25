package org.rocs.asa.service.email;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Properties;

import static org.rocs.asa.domain.email.constant.EmailConstant.*;

/**
 * Service for sending email notifications
 */
@Service
public class EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    /**
     * Sends password reset verification email
     * User must click the link to complete the password change
     *
     * @param email recipient email address
     * @param verifyUrl verification link
     */
    public void sendPasswordResetVerificationEmail(String email, String verifyUrl) throws MessagingException {
        Message message = createPasswordResetEmail(email, verifyUrl);

        try (Transport smtpTransport = getEmailSession().getTransport(SIMPLE_MAIL_TRANSFER_PROTOCOL)) {
            smtpTransport.connect(GMAIL_SMTP_SERVER, USERNAME, PASSWORD);
            smtpTransport.sendMessage(message, message.getAllRecipients());
            LOGGER.info("Password reset verification email sent to: {}", email);
        }
    }
    /**
     * Sends New Created Account to user email
     *
     * @param username the username assigned to the new user
     * @param password the temporary password generated for the user
     */
    public void sendNewRegisterAccountEmail(String email,String username,String password) throws MessagingException {
        Message message = createNewRegisterAccountEmail(email,username,password);
        try (Transport smtpTransport = getEmailSession().getTransport(SIMPLE_MAIL_TRANSFER_PROTOCOL)) {
            smtpTransport.connect(GMAIL_SMTP_SERVER, USERNAME, PASSWORD);
            smtpTransport.sendMessage(message, message.getAllRecipients());
            LOGGER.info("New created account sent to: {}", email);
        }
    }

    private Message createNewRegisterAccountEmail(String email,String username,String password) throws MessagingException {
        Message message = new MimeMessage(getEmailSession());
        message.setFrom(new InternetAddress(FROM_EMAIL));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(email));
        message.setSubject(EMAIL_STAFF_NEW_REGISTRATION_SUBJECT);

        String emailBody = registrationEmailBody(username,password);
        message.setText(emailBody);
        message.setSentDate(new Date());
        message.saveChanges();

        return message;
    }

    private Message createPasswordResetEmail(String email, String verifyUrl)
            throws MessagingException {

        Message message = new MimeMessage(getEmailSession());
        message.setFrom(new InternetAddress(FROM_EMAIL));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(email));
        message.setSubject(EMAIL_SUBJECT);

        String emailBody = buildEmailBody(verifyUrl);
        message.setText(emailBody);
        message.setSentDate(new Date());
        message.saveChanges();

        return message;
    }
    private String registrationEmailBody(String username, String password) {
        return "Dear User,\n\n"
                + "Your account has been successfully created by the Management Information System (MIS).\n\n"
                + "Here are your login credentials:\n"
                + "Username: " + username + "\n"
                + "Password: " + password + "\n\n"
                + "For security purposes, please change your password after your first login.\n\n";
    }

    private String buildEmailBody(String verifyUrl) {
        return "The Lord be with you and good day!\n\n" +
                "You have requested to reset your password.\n\n" +
                "To complete the password reset, please click the link below:\n\n" +
                verifyUrl + "\n\n" +
                "This link will expire in 24 hours.\n\n" +
                "If you did not request this password reset, please ignore this email.\n\n" +
                "MIS Support Team";
    }

    private Session getEmailSession() {
        Properties properties = System.getProperties();
        properties.put(SMTP_HOST, GMAIL_SMTP_SERVER);
        properties.put(SMTP_AUTH, true);
        properties.put(SMTP_PORT, DEFAULT_PORT);
        properties.put(SMTP_STARTTLS_ENABLE, true);
        properties.put(SMTP_STARTTLS_REQUIRED, true);
        return Session.getInstance(properties, null);
    }
}