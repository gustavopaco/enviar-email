package com.pacoprojects.enviaremail.email.details;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailDetails {

    // Para:
    private String recipient;
    // Corpo:
    private String msgBody;
    // Assunto:
    private String subject;
    // Anexo:
    private String attachment;


}
