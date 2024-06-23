package it.theapplegeek.spring_starter_pack.service;

import it.theapplegeek.spring_starter_pack.model.User;
import it.theapplegeek.spring_starter_pack.util.email.ResetPasswordEmail;
import it.theapplegeek.spring_starter_pack.util.email.SimpleEmail;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Log
@Service
@RequiredArgsConstructor
public class EmailService {
  @Value("${application.frontend.url}")
  private String frontendUrl;

  @Value("${spring.mail.from}")
  private String from;

  private final JavaMailSender emailSender;
  private final SpringTemplateEngine thymeleafTemplateEngine;
  private final RabbitTemplate rabbitTemplate;

  // ===========================================
  // = SIMPLE EMAIL + HANDLER
  // ===========================================
  public void sendSimpleEmail(SimpleEmail simpleEmail) {
    rabbitTemplate.convertAndSend("x.email", "email.simple-email", simpleEmail);
  }

  @RabbitListener(queues = "q.email.simple-email")
  public void handleTextMail(SimpleEmail simpleEmail) {
    log.info("Sending email: " + simpleEmail.getTo());
    SimpleMailMessage message = new SimpleMailMessage();
    message.setTo(simpleEmail.getTo());
    if (simpleEmail.getCc() != null && !simpleEmail.getCc().isEmpty()) {
      message.setCc(simpleEmail.getCc().toArray(new String[0]));
    }
    if (simpleEmail.getBcc() != null && !simpleEmail.getBcc().isEmpty()) {
      message.setBcc(simpleEmail.getBcc().toArray(new String[0]));
    }
    message.setSubject(simpleEmail.getSubject());
    message.setText(simpleEmail.getText());
    emailSender.send(message);
  }

  // ===========================================
  // = RESET PASSWORD EMAIL + HANDLER
  // ===========================================
  public void sendResetPasswordEmail(User user, String token) {
    ResetPasswordEmail resetPasswordEmail =
        ResetPasswordEmail.builder().user(user).token(token).build();
    rabbitTemplate.convertAndSend("x.email", "email.reset-password", resetPasswordEmail);
  }

  @SneakyThrows
  @RabbitListener(queues = "q.email.reset-password")
  public void handleResetPasswordEmail(ResetPasswordEmail resetPasswordEmail) {
    log.info("Sending email: " + resetPasswordEmail.getUser().getEmail());
    MimeMessage message = emailSender.createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message, true);
    helper.setFrom(from);
    helper.setTo(resetPasswordEmail.getUser().getEmail());
    helper.setSubject("Reset your password - Contract Sender");

    Context thymeleafContext = new Context();
    thymeleafContext.setVariable("name", resetPasswordEmail.getUser().getName());
    thymeleafContext.setVariable(
        "resetLink", frontendUrl + "/reset-password?token=" + resetPasswordEmail.getToken());
    String htmlBody = thymeleafTemplateEngine.process("reset-password.html", thymeleafContext);

    helper.setText(htmlBody, true);
    emailSender.send(message);
  }
}
