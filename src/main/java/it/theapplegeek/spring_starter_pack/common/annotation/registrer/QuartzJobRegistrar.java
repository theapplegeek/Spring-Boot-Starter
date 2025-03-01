package it.theapplegeek.spring_starter_pack.common.annotation.registrer;

import it.theapplegeek.spring_starter_pack.common.annotation.QuartzJob;
import it.theapplegeek.spring_starter_pack.quartz.service.QuartzService;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuartzJobRegistrar implements ApplicationRunner {
  private final Environment env;
  private final Scheduler scheduler;
  private final ApplicationContext applicationContext;
  private final QuartzService quartzService;

  @Override
  @SuppressWarnings("unchecked")
  public void run(ApplicationArguments args) throws Exception {
    Map<String, Object> beans = applicationContext.getBeansWithAnnotation(QuartzJob.class);
    Set<String> activeJobNames = new HashSet<>();

    for (Object bean : beans.values()) {
      if (!Job.class.isAssignableFrom(bean.getClass())) {
        throw new IllegalStateException(
            "Error: Class " + bean.getClass().getName() + " must implement org.quartz.Job.");
      }

      Class<? extends Job> jobClass = (Class<? extends Job>) bean.getClass();
      QuartzJob annotation = jobClass.getAnnotation(QuartzJob.class);
      validateAnnotationParameters(annotation);

      JobKey jobKey = new JobKey(annotation.name(), annotation.group());
      JobDetail jobDetail =
          quartzService.createJobDetail(jobClass, jobKey, annotation.description());
      activeJobNames.add(jobKey.getName());

      if (annotation.jobType() == QuartzJob.JobType.APPLICATION_CONFIG) {
        String cronExpression = getCronExpression(annotation.cronExpressionConfigKey());
        if (scheduler.checkExists(jobKey)) {
          Trigger existingTrigger =
              this.scheduler.getTriggersOfJob(jobKey).stream()
                  .filter(
                      trigger ->
                          trigger.getKey().getName().equals(annotation.name() + "Trigger")
                              && trigger.getKey().getGroup().equals(annotation.group()))
                  .findFirst()
                  .orElse(null);
          if (existingTrigger != null) {
            Trigger newTrigger = quartzService.createTrigger(jobDetail, jobKey, cronExpression);
            this.scheduler.rescheduleJob(existingTrigger.getKey(), newTrigger);
            log.info("Job {} rescheduled", jobKey.getName());
          }
        } else {
          Trigger trigger = quartzService.createTrigger(jobDetail, jobKey, cronExpression);
          this.scheduler.scheduleJob(jobDetail, trigger);
          log.info("Job {} scheduled", jobKey.getName());
        }
      } else if (annotation.jobType() == QuartzJob.JobType.USER_CONFIG) {
        if (!scheduler.checkExists(jobKey)) {
          quartzService.validateCronExpression(annotation.defaultCronExpression());
          String cronExpression = annotation.defaultCronExpression();
          Trigger trigger = quartzService.createTrigger(jobDetail, jobKey, cronExpression);
          this.scheduler.scheduleJob(jobDetail, trigger);
          log.info("Job {} scheduled", jobKey.getName());
        }
      }
    }

    removeObsoleteJobs(activeJobNames);
  }

  private String getCronExpression(String cronExpressionConfigKey) {
    String cronExpression = env.getProperty(cronExpressionConfigKey);
    if (cronExpression == null) {
      throw new IllegalStateException(
          "Cron expression not found for key: " + cronExpressionConfigKey);
    } else if (!CronExpression.isValidExpression(cronExpression)) {
      throw new IllegalStateException(
          "Invalid cron expression: " + cronExpression + " for key: " + cronExpressionConfigKey);
    }
    return cronExpression;
  }

  private void removeObsoleteJobs(Set<String> activeJobNames) throws SchedulerException {
    for (String groupName : scheduler.getJobGroupNames()) {
      for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
        if (!activeJobNames.contains(jobKey.getName())) {
          scheduler.deleteJob(jobKey);
          log.info("Job {} is deleted", jobKey.getName());
        }
      }
    }
  }

  private void validateAnnotationParameters(QuartzJob annotation) {
    if (annotation.name() == null) {
      throw new IllegalStateException("QuartzJob name parameter must be set");
    }
    if (annotation.jobType() == null) {
      throw new IllegalStateException("QuartzJob jobType parameter must be set");
    }
    if (annotation.jobType() == QuartzJob.JobType.APPLICATION_CONFIG) {
      if (annotation.cronExpressionConfigKey() == null) {
        throw new IllegalStateException("QuartzJob cronConfigKey parameter must be set");
      }
    } else if (annotation.jobType() == QuartzJob.JobType.USER_CONFIG) {
      if (annotation.defaultCronExpression() == null) {
        throw new IllegalStateException("QuartzJob defaultCron parameter must be set");
      }
    }
  }
}
