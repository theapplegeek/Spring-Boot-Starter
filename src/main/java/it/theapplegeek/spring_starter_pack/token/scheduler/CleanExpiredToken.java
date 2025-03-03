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
    name = CleanExpiredToken.JOB_NAME,
    group = CleanExpiredToken.JOB_GROUP,
    jobType = QuartzJob.JobType.APPLICATION_CONFIG,
    cronExpressionConfigKey = CleanExpiredToken.CONFIG_KEY)
public class CleanExpiredToken implements Job {
  public static final String JOB_NAME = "cleanExpiredToken";
  public static final String JOB_GROUP = "token";
  public static final String CONFIG_KEY = "application.cron-job.clean-expired-token";

  private final TokenRepository tokenRepository;

  @Override
  public void execute(JobExecutionContext jobExecutionContext) {
    LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
    tokenRepository.deleteAllByExpirationDate(yesterday);
  }
}
