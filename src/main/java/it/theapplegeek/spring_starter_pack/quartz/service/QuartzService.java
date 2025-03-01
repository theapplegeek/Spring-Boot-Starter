package it.theapplegeek.spring_starter_pack.quartz.service;

import it.theapplegeek.spring_starter_pack.common.annotation.QuartzJob;
import it.theapplegeek.spring_starter_pack.common.exception.BadRequestException;
import it.theapplegeek.spring_starter_pack.common.exception.InternalServerErrorException;
import it.theapplegeek.spring_starter_pack.common.exception.NotFoundException;
import it.theapplegeek.spring_starter_pack.quartz.error.QuartzErrorMessage;
import it.theapplegeek.spring_starter_pack.quartz.payload.JobInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import lombok.AllArgsConstructor;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class QuartzService {
  private final Scheduler scheduler;

  public List<JobInfo> getJobs() {
    List<JobInfo> jobList = new ArrayList<>();

    try {
      for (String groupName : scheduler.getJobGroupNames()) {
        for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {

          JobDetail jobDetail = scheduler.getJobDetail(jobKey);
          QuartzJob annotation = jobDetail.getJobClass().getAnnotation(QuartzJob.class);

          if (annotation != null && annotation.jobType() == QuartzJob.JobType.USER_CONFIG) {
            TriggerKey triggerKey = new TriggerKey(jobKey.getName() + "Trigger", groupName);
            Trigger trigger = scheduler.getTrigger(triggerKey);

            String cronExpression = "N/A";
            if (trigger instanceof CronTrigger) {
              cronExpression = ((CronTrigger) trigger).getCronExpression();
            }

            jobList.add(
                JobInfo.builder()
                    .name(jobKey.getName())
                    .group(groupName)
                    .cronExpression(cronExpression)
                    .build());
          }
        }
      }
    } catch (SchedulerException e) {
      throw new InternalServerErrorException(QuartzErrorMessage.GET_JOB_LIST_ERROR);
    }

    return jobList;
  }

  public void rescheduleJob(String jobName, String jobGroup, String cronExpression) {
    try {
      JobKey jobKey = new JobKey(jobName, jobGroup);

      if (!scheduler.checkExists(jobKey)) {
        throw new NotFoundException(QuartzErrorMessage.JOB_NOT_FOUND);
      }

      JobDetail jobDetail = scheduler.getJobDetail(jobKey);
      QuartzJob annotation = jobDetail.getJobClass().getAnnotation(QuartzJob.class);

      validateAnnotation(annotation);
      validateCronExpression(cronExpression);

      Trigger existingTrigger =
          this.scheduler.getTriggersOfJob(jobKey).stream()
              .filter(
                  trigger ->
                      trigger.getKey().getName().equals(jobName + "Trigger")
                          && trigger.getKey().getGroup().equals(jobGroup))
              .findFirst()
              .orElse(null);
      if (existingTrigger != null) {
        Trigger newTrigger = createTrigger(jobDetail, jobKey, cronExpression);
        this.scheduler.rescheduleJob(existingTrigger.getKey(), newTrigger);
      } else {
        throw new BadRequestException(QuartzErrorMessage.TRIGGER_NOT_FOUND);
      }
    } catch (SchedulerException e) {
      throw new InternalServerErrorException(QuartzErrorMessage.SCHEDULE_JOB_ERROR);
    }
  }

  private static void validateAnnotation(QuartzJob annotation) {
    if (annotation == null) {
      throw new InternalServerErrorException(QuartzErrorMessage.JOB_NOT_MANAGED);
    }

    if (annotation.jobType() == null || annotation.jobType() != QuartzJob.JobType.USER_CONFIG) {
      throw new BadRequestException(QuartzErrorMessage.JOB_CANT_BE_RESCHEDULED);
    }
  }

  public <T extends Job> JobDetail createJobDetail(
      Class<T> jobClass, JobKey jobKey, String description) {
    return JobBuilder.newJob(jobClass)
        .withIdentity(jobKey)
        .withDescription(description)
        .storeDurably()
        .build();
  }

  public Trigger createTrigger(JobDetail jobDetail, JobKey jobKey, String cronExpression) {
    return TriggerBuilder.newTrigger()
        .forJob(jobDetail)
        .withIdentity(jobKey.getName() + "Trigger", jobKey.getGroup())
        .withSchedule(
            CronScheduleBuilder.cronSchedule(cronExpression)
                .inTimeZone(TimeZone.getTimeZone("Europe/Rome")))
        .build();
  }

  public void validateCronExpression(String cronExpression) {
    if (cronExpression == null) {
      throw new IllegalStateException("Cron expression cannot be null");
    } else if (!CronExpression.isValidExpression(cronExpression)) {
      throw new IllegalStateException("Invalid cron expression: " + cronExpression);
    }
  }
}
