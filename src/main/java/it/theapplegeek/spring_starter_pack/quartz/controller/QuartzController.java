package it.theapplegeek.spring_starter_pack.quartz.controller;

import it.theapplegeek.spring_starter_pack.quartz.payload.JobInfo;
import it.theapplegeek.spring_starter_pack.quartz.service.QuartzService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quartz/job")
@AllArgsConstructor
@CrossOrigin(origins = "*")
public class QuartzController {
  private final QuartzService quartzService;

  @GetMapping
  @PreAuthorize("hasAnyAuthority('QUARTZ_JOB_READ')")
  public List<JobInfo> getJobs() {
    return quartzService.getJobs();
  }

  @PostMapping("reschedule")
  @PreAuthorize("hasAnyAuthority('QUARTZ_JOB_UPDATE')")
  public void rescheduleJob(
      @RequestParam("jobName") String jobName,
      @RequestParam(value = "jobGroup", defaultValue = "DEFAULT") String jobGroup,
      @RequestParam("cronExpression") String cronExpression) {
    quartzService.rescheduleJob(jobName, jobGroup, cronExpression);
  }
}
