package com.pacoprojects.enviaremail.email.details;

public interface EmailService {

    // Method
    // To send a simple email
    void sendSimpleMail(EmailDetails emailDetails);

    // Method
    // To send an email with attachment
    void sendMailWithAttachment(EmailDetails emailDetails);
}
