package com.johnson.blog.service;

import java.util.Map;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
  private final JavaMailSender javaMailSender;

  private final TemplateEngine templateEngine;

  public EmailService(JavaMailSender javaMailSender,
      TemplateEngine templateEngine) {
    this.javaMailSender = javaMailSender;
    this.templateEngine = templateEngine;
  }

  public void sendHtmlEmail(String to, String templateName, String subject, Map<String, Object> variables)
      throws MessagingException {
    Context context = new Context();
    context.setVariables(variables);

    MimeMessage mimeMessage = javaMailSender.createMimeMessage();
    MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(
        mimeMessage,
        true,
        "UTF-8");

    String htmlContent = templateEngine.process(templateName, context);

    mimeMessageHelper.setTo(to);
    mimeMessageHelper.setSubject(subject);
    mimeMessageHelper.setText(htmlContent, true);

    javaMailSender.send(mimeMessage);
  }
}
