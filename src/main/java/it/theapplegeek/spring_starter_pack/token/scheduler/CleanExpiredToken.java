package it.theapplegeek.spring_starter_pack.token.scheduler;

import it.theapplegeek.spring_starter_pack.token.repository.TokenRepository;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Log
public class CleanExpiredToken {
  private final TokenRepository tokenRepository;

  @Scheduled(cron = "0 3 1 * * *", zone = "Europe/Rome")
  public void cleanOldToken() {
    log.info("Start clean old then 1 day token");

    LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
    tokenRepository.deleteAllByExpirationDate(yesterday);

    log.info("End clean old then 1 day token");
  }
}
