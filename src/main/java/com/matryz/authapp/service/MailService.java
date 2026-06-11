package com.matryz.authapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private final JavaMailSender mailSender;
    private final String baseUrl;

    public MailService(JavaMailSender mailSender,
                       @Value("${app.base-url}") String baseUrl) {
        this.mailSender = mailSender;
        this.baseUrl = baseUrl;
    }

    public void sendConfirmationEmail(String to, String name, String token) {
        String link = baseUrl + "/auth/confirm?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Confirme sua conta - Projeto Alpha");
        message.setText(
                "Olá, " + name + "!\n\n" +
                        "Bem-vindo! Para ativar sua conta, clique no link abaixo:\n\n" +
                        link + "\n\n" +
                        "Se você não se cadastrou, ignore este e-mail."
        );

        mailSender.send(message);
    }
}