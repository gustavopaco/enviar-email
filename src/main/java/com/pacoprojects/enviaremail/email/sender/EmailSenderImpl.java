package com.pacoprojects.enviaremail.email.sender;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
@AllArgsConstructor
public class EmailSenderImpl implements EmailSender {

    private final JavaMailSenderImpl emailSender;
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailSenderImpl.class);
    private static final String EMAIL_USERNAME = "";
    private static final String EMAIL_PASSWORD = "";

    @Override
    @Async
    public void send(String to, String emailBody) {

        try {
            emailSender.setHost("smtp.gmail.com");
            emailSender.setUsername(EMAIL_USERNAME);
            emailSender.setPassword(EMAIL_PASSWORD);
            emailSender.setPort(465);
            Properties properties = new Properties();
            properties.put("mail.smtp.starttls", "true"); // Autenticacao
            properties.put("mail.smtp.auth", true);
            properties.put("mail.smtp.socketFactory.port", 465);
            properties.put("mail.smtp.ssl.checkserveridentity", true);
            properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

            emailSender.setJavaMailProperties(properties);

            MimeMessage mimeMessage = emailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");

            /* Corpo do Email*/
            helper.setText(emailBody, true);

            /* Destinatario*/
            helper.setTo(to);

            /* Assunto do Email */
            helper.setSubject("Confirm your email");

            helper.setFrom("gustavopacotest@gmail.com");

            emailSender.send(mimeMessage);

        } catch (MessagingException exception) {
            LOGGER.error("Failed to send email", exception);
            throw new IllegalStateException("Failed to send email");
        }
    }
}
