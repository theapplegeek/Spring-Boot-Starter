package it.theapplegeek.spring_starter_pack.email.service;

import com.rabbitmq.client.Channel;
import it.theapplegeek.spring_starter_pack.common.configuration.RabbitMqConfig;
import it.theapplegeek.spring_starter_pack.email.model.ResetPasswordEmail;
import it.theapplegeek.spring_starter_pack.email.model.SimpleEmail;
import it.theapplegeek.spring_starter_pack.user.model.User;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.messaging.handler.annotation.Header;
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

  @Value("${spring.application.name}")
  private String applicationName;

  private final JavaMailSender emailSender;
  private final SpringTemplateEngine thymeleafTemplateEngine;
  private final RabbitTemplate rabbitTemplate;

  // ===========================================
  // = SIMPLE EMAIL + HANDLER
  // ===========================================
  public void sendSimpleEmail(SimpleEmail simpleEmail) {
    rabbitTemplate.convertAndSend("x.email", "email.simple-email", simpleEmail);
  }

  @SneakyThrows
  @RabbitListener(queues = "q.email.simple-email", ackMode = "MANUAL")
  public void handleTextMail(
      SimpleEmail simpleEmail, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
    try {
      log.info("Sending email: " + simpleEmail.getTo());
      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(from);
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
      channel.basicAck(tag, false);
    } catch (Exception e) {
      log.warning("Error sending email: " + e.getMessage());
      channel.basicNack(tag, false, false);
      throw e;
    }
  }

  // ===========================================
  // = RESET PASSWORD EMAIL + HANDLER
  // ===========================================
  public void sendResetPasswordEmail(User user, String token) {
    ResetPasswordEmail resetPasswordEmail =
        ResetPasswordEmail.builder()
            .email(user.getEmail())
            .name(user.getName())
            .token(token)
            .build();
    rabbitTemplate.convertAndSend("x.email", "email.reset-password", resetPasswordEmail);
  }

  @SneakyThrows
  @RabbitListener(queues = RabbitMqConfig.RESET_PASSWORD_QUEUE, ackMode = "MANUAL")
  public void handleResetPasswordEmail(
      ResetPasswordEmail resetPasswordEmail,
      Channel channel,
      @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
    try {
      log.info("Sending email: " + resetPasswordEmail.getEmail());
      MimeMessage message = emailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);
      helper.setFrom(from);
      helper.setTo(resetPasswordEmail.getEmail());
      helper.setSubject("Reset your password - " + applicationName);

      Context thymeleafContext = new Context();
      thymeleafContext.setVariable("name", resetPasswordEmail.getName());
      thymeleafContext.setVariable("applicationName", applicationName);
      thymeleafContext.setVariable(
          "resetLink", frontendUrl + "/reset-password?token=" + resetPasswordEmail.getToken());
      String htmlBody = thymeleafTemplateEngine.process("reset-password.html", thymeleafContext);

      helper.setText(htmlBody, true);
      emailSender.send(message);
      channel.basicAck(tag, false);
    } catch (Exception e) {
      log.warning("Error sending email: " + e.getMessage());
      channel.basicNack(tag, false, false);
      throw e;
    }
  }
}
