package it.theapplegeek.spring_starter_pack.quartz.listenner;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class QuartzJobLoggerListener implements JobListener {
  @Override
  public String getName() {
    return "QuartzJobLoggerListener";
  }

  @Override
  public void jobToBeExecuted(JobExecutionContext context) {
    log.info(
        "Job {} starting (Trigger: '{}')",
        context.getJobDetail().getKey(),
        context.getTrigger().getKey());
  }

  @Override
  public void jobExecutionVetoed(JobExecutionContext context) {
    log.warn("Job {} is VETOED, skipping execution!", context.getJobDetail().getKey());
  }

  @Override
  public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
    if (jobException == null) {
      log.info("Job {} is COMPLETED successfully!", context.getJobDetail().getKey());
    } else {
      log.error(
          "Job {} is FAILED with error: {}",
          context.getJobDetail().getKey(),
          jobException.getMessage());
    }
  }
}
