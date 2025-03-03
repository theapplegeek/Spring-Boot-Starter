package it.theapplegeek.spring_starter_pack.token.scheduler;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
@AutoConfigureTestDatabase
@ExtendWith(MockitoExtension.class)
class CleanExpiredTokenIntegrationTest {
  @Container @ServiceConnection
  static PostgreSQLContainer<?> postgreSQLContainer =
      new PostgreSQLContainer<>("postgres:16.3-alpine")
          .withUsername("myuser")
          .withPassword("Password1!")
          .withInitScript("db/data.sql");
  
  @Autowired private Scheduler scheduler;

  @BeforeEach
  void setUp() throws SchedulerException {
    scheduler.start();
  }

  @Test
  void shouldRunJobInSchedulerWithCorrectCron() throws SchedulerException {
    JobKey jobKey = new JobKey(CleanExpiredToken.JOB_NAME, CleanExpiredToken.JOB_GROUP);
    assertTrue(scheduler.checkExists(jobKey), "Job should be scheduled!");

    List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
    assertFalse(triggers.isEmpty(), "Job should have triggers!");

    CronTrigger cronTrigger = (CronTrigger) triggers.getFirst();
    assertEquals(
        "0 3 1 * * ? *", cronTrigger.getCronExpression(), "Cron should match expected value!");
  }
}
