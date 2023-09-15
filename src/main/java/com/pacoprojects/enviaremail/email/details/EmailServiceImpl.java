package com.pacoprojects.enviaremail.email.details;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;

    @Value(value = "${spring.mail.username}")
    private String sender;

    // Method 1
    // To send a simple email
    @Override
    @Async
    public void sendSimpleMail(EmailDetails details) {

        // Try block to check for exceptions
        try {

            // Creating a simple mail message
            SimpleMailMessage mailMessage = new SimpleMailMessage();

            // Setting up necessary details
            // De:
            mailMessage.setFrom(sender);
            // Para:
            mailMessage.setTo(details.getRecipient());
            // Assunto:
            mailMessage.setSubject(details.getSubject());
            // Corpo:
            mailMessage.setText(details.getMsgBody());

            // Sending the mail
            javaMailSender.send(mailMessage);

            // Metodo deve ser VOID para conseguir funcionar de modo Asyncrono
            // return "Mail Sent Successfully...";
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Error while Sending mail");
        }
    }

    @Override
    @Async
    public void sendMailWithAttachment(EmailDetails details) {

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper;

        try {
            // Setting multipart as true for attachments to
            // be send
            helper = new MimeMessageHelper(mimeMessage, true);

            // Setting up necessary details
            // De:
            helper.setFrom(sender);
            // Para:
            helper.setTo(details.getRecipient());
            // Assunto:
            helper.setSubject(details.getSubject());
            // Corpo HTML:
            helper.setText(details.getMsgBody(), true);

            // Adding the attachment
            if (details.getAttachment() != null && !details.getAttachment().isBlank()) {
                FileSystemResource file = new FileSystemResource(details.getAttachment());
                helper.addAttachment(Objects.requireNonNull(file.getFilename()), file);
            }

            // Sending the mail
            javaMailSender.send(mimeMessage);

            // Metodo deve ser VOID para conseguir funcionar de modo Asyncrono
            // return "Mail sent Successfully";

            // Catch block to handle MessagingException
        } catch (MessagingException e) {
            // Display message when exception occurred
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Error while sending mail!!!");
        }
    }
}
