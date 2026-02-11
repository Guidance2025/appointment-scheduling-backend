package org.rocs.asa.service.email;

import org.springframework.beans.factory.annotation.Value;
import sendinblue.ApiClient;
import sendinblue.Configuration;
import sendinblue.auth.ApiKeyAuth;
import sibApi.TransactionalEmailsApi;
import sibModel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    @Value("${brevo.from.email}")
    private String fromEmail;

    @Value("${brevo.from.name}")
    private String fromName;

    public void sendPasswordResetVerificationEmail(String email, String verifyUrl) throws Exception {
        String subject = "Password Reset Verification - GABAY";
        String body = "The Lord be with you and good day!\n\n"
                + "You have requested to reset your password.\n\n"
                + "To complete the password reset, please click the link below:\n\n"
                + verifyUrl + "\n\n"
                + "This link will expire in 24 hours.\n\n"
                + "If you did not request this password reset, please ignore this email.\n\n"
                + "MIS Support Team";

        sendEmail(email, subject, body);
        LOGGER.info("Password reset verification email sent to: {}", email);
    }

    public void sendNewRegisterAccountEmail(String email, String username, String password) throws Exception {
        String subject = "New Account Created - GABAY";
        String body = "The Lord be with you and good day!\n\n"
                + "Your account has been successfully created by the Management Information System (MIS).\n\n"
                + "Here are your login credentials:\n"
                + "Username: " + username + "\n"
                + "Password: " + password + "\n\n"
                + "For security purposes, please change your password after your first login.\n\n";

        sendEmail(email, subject, body);
        LOGGER.info("New created account sent to: {}", email);
    }

    public void sendStudentNewPasswordEmail(String email, String newPassword) throws Exception {
        String subject = "Password Changed - GABAY";
        String body = "The Lord be with you and good day!\n\n"
                + "Your Password has been successfully changed by the Management Information System (MIS).\n\n"
                + "Here are your new password:\n"
                + "New Password: " + newPassword + "\n\n";

        sendEmail(email, subject, body);
        LOGGER.info("New Password has been sent to: {}", email);
    }

    private void sendEmail(String toEmail, String subject, String body) throws Exception {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        ApiKeyAuth apiKey = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
        apiKey.setApiKey(brevoApiKey);

        TransactionalEmailsApi api = new TransactionalEmailsApi();

        SendSmtpEmailSender sender = new SendSmtpEmailSender();
        sender.setEmail(fromEmail);
        sender.setName(fromName);

        SendSmtpEmailTo recipient = new SendSmtpEmailTo();
        recipient.setEmail(toEmail);

        SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();
        sendSmtpEmail.setSender(sender);
        sendSmtpEmail.setTo(Collections.singletonList(recipient));
        sendSmtpEmail.setSubject(subject);
        sendSmtpEmail.setTextContent(body);

        CreateSmtpEmail result = api.sendTransacEmail(sendSmtpEmail);
        LOGGER.info("Email sent successfully, messageId: {}", result.getMessageId());
    }
}