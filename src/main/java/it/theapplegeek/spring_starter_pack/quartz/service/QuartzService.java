package it.theapplegeek.spring_starter_pack.quartz.service;

import it.theapplegeek.spring_starter_pack.common.annotation.QuartzJob;
import it.theapplegeek.spring_starter_pack.common.exception.BadRequestException;
import it.theapplegeek.spring_starter_pack.common.exception.InternalServerErrorException;
import it.theapplegeek.spring_starter_pack.common.exception.NotFoundException;
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
      throw new InternalServerErrorException("Error on get jobs");
    }

    return jobList;
  }

  public void rescheduleJob(String jobName, String jobGroup, String cronExpression) {
    try {
      JobKey jobKey = new JobKey(jobName, jobGroup);

      if (!scheduler.checkExists(jobKey)) {
        throw new NotFoundException("Job " + jobGroup + "." + jobName + " not found");
      }

      JobDetail jobDetail = scheduler.getJobDetail(jobKey);
      QuartzJob annotation = jobDetail.getJobClass().getAnnotation(QuartzJob.class);

      validateAnnotation(jobName, jobGroup, annotation);
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
        throw new BadRequestException("Trigger not found");
      }
    } catch (SchedulerException e) {
      throw new InternalServerErrorException("Error on reschedule job " + jobGroup + "." + jobName);
    }
  }

  private static void validateAnnotation(String jobName, String jobGroup, QuartzJob annotation) {
    if (annotation == null) {
      throw new InternalServerErrorException(
          "Job " + jobGroup + "." + jobName + " is not managed by QuartzJob");
    }

    if (annotation.jobType() == null || annotation.jobType() != QuartzJob.JobType.USER_CONFIG) {
      throw new BadRequestException("Job " + jobGroup + "." + jobName + " can't be rescheduled");
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
