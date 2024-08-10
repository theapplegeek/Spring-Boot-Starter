package it.theapplegeek.spring_starter_pack.common.configuration;

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

  // Exchanges
  public static final String EMAIL_EXCHANGE = "x.email";

  // Queues
  public static final String SIMPLE_EMAIL_QUEUE = "q.email.simple-email";
  public static final String SIMPLE_EMAIL_DELAY_QUEUE = "q.email.simple-email.delay";
  public static final String RESET_PASSWORD_QUEUE = "q.email.reset-password";
  public static final String RESET_PASSWORD_DELAY_QUEUE = "q.email.reset-password.delay";

  // Routing keys
  public static final String SIMPLE_EMAIL_ROUTING_KEY = "email.simple-email";
  public static final String SIMPLE_EMAIL_DELAY_ROUTING_KEY = "email.simple-email.delay";
  public static final String RESET_PASSWORD_ROUTING_KEY = "email.reset-password";
  public static final String RESET_PASSWORD_DELAY_ROUTING_KEY = "email.reset-password.delay";

  // =======================================
  // = EXCHANGES
  // =======================================
  @Bean
  public DirectExchange emailExchange() {
    return new DirectExchange(EMAIL_EXCHANGE);
  }

  // =======================================
  // = QUEUES
  // =======================================
  @Bean
  public Queue simpleEmailQueue() {
    return QueueBuilder.durable(SIMPLE_EMAIL_QUEUE)
        .withArgument("x-dead-letter-exchange", EMAIL_EXCHANGE)
        .withArgument("x-dead-letter-routing-key", SIMPLE_EMAIL_DELAY_ROUTING_KEY)
        .build();
  }

  @Bean
  public Queue simpleEmailDelayQueue() {
    return QueueBuilder.durable(SIMPLE_EMAIL_DELAY_QUEUE)
        .withArgument("x-dead-letter-exchange", EMAIL_EXCHANGE)
        .withArgument("x-dead-letter-routing-key", SIMPLE_EMAIL_ROUTING_KEY)
        .withArgument("x-message-ttl", 300000)
        .build();
  }

  @Bean
  public Queue resetPasswordQueue() {
    return QueueBuilder.durable(RESET_PASSWORD_QUEUE)
        .withArgument("x-dead-letter-exchange", EMAIL_EXCHANGE)
        .withArgument("x-dead-letter-routing-key", RESET_PASSWORD_DELAY_ROUTING_KEY)
        .build();
  }

  @Bean
  public Queue resetPasswordDelayQueue() {
    return QueueBuilder.durable(RESET_PASSWORD_DELAY_QUEUE)
        .withArgument("x-dead-letter-exchange", EMAIL_EXCHANGE)
        .withArgument("x-dead-letter-routing-key", RESET_PASSWORD_ROUTING_KEY)
        .withArgument("x-message-ttl", 300000)
        .build();
  }

  // =======================================
  // = BINDINGS
  // =======================================
  @Bean
  public Binding simpleEmailBinding(DirectExchange emailExchange, Queue simpleEmailQueue) {
    return BindingBuilder.bind(simpleEmailQueue).to(emailExchange).with(SIMPLE_EMAIL_ROUTING_KEY);
  }

  @Bean
  public Binding simpleEmailDelayBinding(
      DirectExchange emailExchange, Queue simpleEmailDelayQueue) {
    return BindingBuilder.bind(simpleEmailDelayQueue)
        .to(emailExchange)
        .with(SIMPLE_EMAIL_DELAY_ROUTING_KEY);
  }

  @Bean
  public Binding resetPasswordBinding(DirectExchange emailExchange, Queue resetPasswordQueue) {
    return BindingBuilder.bind(resetPasswordQueue)
        .to(emailExchange)
        .with(RESET_PASSWORD_ROUTING_KEY);
  }

  @Bean
  public Binding resetPasswordDelayBinding(
      DirectExchange emailExchange, Queue resetPasswordDelayQueue) {
    return BindingBuilder.bind(resetPasswordDelayQueue)
        .to(emailExchange)
        .with(RESET_PASSWORD_DELAY_ROUTING_KEY);
  }

  // =======================================
  // = CUSTOM CONVERTER
  // =======================================
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
