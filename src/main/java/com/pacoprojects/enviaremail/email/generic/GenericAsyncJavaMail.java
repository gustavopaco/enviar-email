package com.pacoprojects.enviaremail.email.generic;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.activation.DataHandler;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Properties;

@Component
public class GenericAsyncJavaMail {

    private static final String HTML_CONTENT = "text/html; charset=utf-8";

    private Properties getGeneralProperties(String tipoSMTP) {

        Properties properties = new Properties();
        if (tipoSMTP.equalsIgnoreCase("gmail")) {
            properties.put("mail.ssl.trust", "*");
            properties.put("mail.smtp.trust", "*");
            properties.put("mail.smtp.auth", "true"); // Autorizacao
            properties.put("mail.smtp.starttls", "true"); // Autenticacao
            properties.put("mail.smtp.host", "smtp.gmail.com"); // Servidor do Google Gmail
            properties.put("mail.smtp.port", 465); // Porta do servidor do Google
            properties.put("mail.smtp.socketFactory.port", 465); // Expecifica a porta a ser conectada pelo socket
            properties.put("mail.smtp.ssl.checkserveridentity", "true"); // Verifica se o servidor eh valido
            properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory"); // Classe socket de conexao ao SMTP
        }

        return properties;
    }

    @Async
    public void enviarEmail(String emailRemetente, String passwordRemetente,
                            String nomeRemetente,List<String> listaEmailsDestinatarios,
                            String assuntoEmail, String conteudoEmail,
                            boolean isHTML, List<String> anexosBase64)
            throws Exception {

        String tipoSMTPRemetente =  emailRemetente.substring((emailRemetente.indexOf("@")+1),emailRemetente.indexOf(".com"));

        // Olhar as configuracoes SMTP do seu email
        Properties properties = getGeneralProperties(tipoSMTPRemetente);

        Session session = Session.getInstance(properties, new Authenticator() {

            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailRemetente, passwordRemetente);
            }
        });

        StringBuilder destinatarios = new StringBuilder();

        if (listaEmailsDestinatarios.size() > 1) {
            for (int i = 0; i < listaEmailsDestinatarios.size(); i++) {

                destinatarios.append(listaEmailsDestinatarios.get(i));
                if (i < listaEmailsDestinatarios.size() - 1) {
                    destinatarios.append(",");
                }
            }
        } else {
            destinatarios.append(listaEmailsDestinatarios.get(0));
        }

        Address[] toUser = InternetAddress.parse(destinatarios.toString()); // Setando o Email ou a Lista de Emails que sera enviado, SEPARDO POR ","

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(emailRemetente, nomeRemetente)); // Email de quem ta enviando + Nome da empresa que ira aparecer na Caixa de Entrada
        message.setRecipients(Message.RecipientType.TO, toUser); // Para quem esta enviando
        message.setSubject(assuntoEmail); // Assunto do email

        /* Parte 1 do e-mail - Descricao do email */
        MimeBodyPart corpoEmail = new MimeBodyPart();

        if (isHTML) {
            corpoEmail.setContent(conteudoEmail, HTML_CONTENT);
        } else {
            corpoEmail.setText(conteudoEmail);
        }

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(corpoEmail);

        if (!anexosBase64.isEmpty()) {
            String fileBase64;
            String fileMIMEType;
            String fileType;
            for (String itemBase64 : anexosBase64) {

                /* Obtendo fileBase64, MimeType e FileType */
                fileBase64 = itemBase64.substring(itemBase64.indexOf(",")+1);
                byte[] fileBase64InBytes = Base64.getDecoder().decode(fileBase64);
                fileMIMEType = itemBase64.substring(itemBase64.indexOf(":")+1, itemBase64.indexOf(";"));
                fileType = "." + itemBase64.substring(itemBase64.indexOf("/")+1, itemBase64.indexOf(";"));

                /* Parte 2 do e-mail - Sao os anexos em PDF */
                MimeBodyPart anexoEmail = new MimeBodyPart();

                /* Onde eh passado o simulador de PDF, voce passa seu arquivo gravado no Banco de dados
                 *  O arquivo passado pode ser:
                 *  (InputStream, Tipo do arquivo(formato)) ou (byte[], Tipo do arquivo(formato)) ou (String Base64, Tipo do arquivo(formato)) */
//                anexoEmail.setDataHandler(new DataHandler(new ByteArrayDataSource(fileInputStream, "application/pdf")));
                anexoEmail.setDataHandler(new DataHandler(new ByteArrayDataSource(fileBase64InBytes,fileMIMEType)));
                anexoEmail.setFileName("aquivo" + ((int) (Math.random() * 1000) + 1) + fileType);

                /* Parte 3 do e-mail - Juntando corpo do email com o anexo em PDF*/
                multipart.addBodyPart(anexoEmail);
            }
        }
        /* Parte 4 do e-mail - Setando na mensagem o objeto completo com (corpo do email + anexo) */
        message.setContent(multipart);

        // Enviar o Email
        Transport.send(message);

        // Caso o email nao esteja sendo enviado, coloque um tempo de espera, mas isso so pode ser usado para Testes...
        // Thread.sleep(5000);
    }

    // Esse metodo simula o PDF ou qualquer arquivo que possa ser enviado por anexo no email.
    // O arquivo pode ser pego em um banco de dados, ou em uma pasta, etc.
    // Retorna um PDF em branco com o texto do paragrafo do exemplo...
    // Lib itextpdf.text
    private FileInputStream simuladorPDF() throws IOException, DocumentException {
        Document document = new Document();
        File file = new File("fileAnexo.pdf");
        file.createNewFile();
        PdfWriter.getInstance(document, new FileOutputStream(file));
        document.open();
        document.add(new Paragraph("Conteudo do PDF anexo com Java Mail, usando API itext"));
        document.close();

        return new FileInputStream(file);
    }
}
