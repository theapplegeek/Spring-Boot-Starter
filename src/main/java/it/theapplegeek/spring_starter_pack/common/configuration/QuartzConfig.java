package it.theapplegeek.spring_starter_pack.common.configuration;

import it.theapplegeek.spring_starter_pack.quartz.listenner.QuartzJobLoggerListener;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@AllArgsConstructor
public class QuartzConfig {
  private final Scheduler scheduler;
  private final QuartzJobLoggerListener jobLoggerListener;

  @PostConstruct
  public void registerJobListener() {
    try {
      scheduler.getListenerManager().addJobListener(jobLoggerListener);
    } catch (SchedulerException e) {
      log.error("Error on register job listener", e);
    }
  }
}
