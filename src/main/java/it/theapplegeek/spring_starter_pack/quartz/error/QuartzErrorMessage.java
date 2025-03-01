package it.theapplegeek.spring_starter_pack.quartz.error;

public class QuartzErrorMessage {
  public static final String JOB_NOT_FOUND = "http.error.job.notFound";
  public static final String TRIGGER_NOT_FOUND = "http.error.trigger.notFound";
  public static final String SCHEDULE_JOB_ERROR = "http.error.job.scheduleError";
  public static final String JOB_NOT_MANAGED = "http.error.job.notManaged";
  public static final String JOB_CANT_BE_RESCHEDULED = "http.error.job.cantBeRescheduled";
  public static final String GET_JOB_LIST_ERROR = "http.error.job.getJobListError";

  private QuartzErrorMessage() {}
}
