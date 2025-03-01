package it.theapplegeek.spring_starter_pack.token.scheduler;

import it.theapplegeek.spring_starter_pack.common.annotation.QuartzJob;
import it.theapplegeek.spring_starter_pack.token.repository.TokenRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@DisallowConcurrentExecution
@QuartzJob(
    name = "cleanExpiredToken",
    group = "token",
    jobType = QuartzJob.JobType.APPLICATION_CONFIG,
    cronExpressionConfigKey = "application.cron-job.clean-expired-token")
public class CleanExpiredToken implements Job {
  private final TokenRepository tokenRepository;

  @Override
  public void execute(JobExecutionContext jobExecutionContext) {
    LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
    tokenRepository.deleteAllByExpirationDate(yesterday);
  }
}
