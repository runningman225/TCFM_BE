package com.future.tcfm.config.mail;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {
    private static final String MY_EMAIL = "future.medan2@gmail.com";
    private static final String MY_PASSWORD = "futuregdn";

    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);

        mailSender.setUsername(MY_EMAIL);
        mailSender.setPassword(MY_PASSWORD);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.mime.address.strict","false");
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth","false");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");
        //Mail properties that are needed to specify e.g. the SMTP server may be defined using the JavaMailSenderImpl. For example, for Gmail this can be configured as shown
        return mailSender;
    }

}