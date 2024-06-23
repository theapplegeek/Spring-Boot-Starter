package it.theapplegeek.spring_starter_pack.configuration;

import lombok.AllArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class RabbitMqConfig {
  private final CachingConnectionFactory cachingConnectionFactory;

  @Bean
  public Declarables createEmailSchema() {
    return new Declarables(
        new DirectExchange("x.email"),
        new Queue("q.email.simple-email"),
        new Queue("q.email.reset-password"),
        new Binding(
            "q.email.simple-email",
            Binding.DestinationType.QUEUE,
            "x.email",
            "email.simple-email",
            null),
        new Binding(
            "q.email.reset-password",
            Binding.DestinationType.QUEUE,
            "x.email",
            "email.reset-password",
            null));
  }

  @Bean
  public Jackson2JsonMessageConverter converter() {
    return new Jackson2JsonMessageConverter();
  }

  @Bean
  public RabbitTemplate rabbitTemplate(Jackson2JsonMessageConverter converter) {
    RabbitTemplate rabbitTemplate = new RabbitTemplate(cachingConnectionFactory);
    rabbitTemplate.setMessageConverter(converter);
    return rabbitTemplate;
  }
}
