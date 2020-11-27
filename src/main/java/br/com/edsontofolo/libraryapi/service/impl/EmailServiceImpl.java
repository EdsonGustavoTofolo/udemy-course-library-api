package br.com.edsontofolo.libraryapi.service.impl;

import br.com.edsontofolo.libraryapi.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    @Value("${application.mail.default-sender}")
    private String sender;

    private final JavaMailSender javaMailSender;

    @Override
    public void sendMails(String message, List<String> emails) {
        String[] emailsTo = emails.toArray(new String[emails.size()]);

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(sender);
        mailMessage.setSubject("Livro com devolução atrasada");
        mailMessage.setText(message);
        mailMessage.setTo(emailsTo);

        javaMailSender.send(mailMessage);
    }
}
