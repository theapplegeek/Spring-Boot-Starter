package it.theapplegeek.spring_starter_pack.token.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import it.theapplegeek.spring_starter_pack.token.repository.TokenRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobExecutionContext;

@ExtendWith(MockitoExtension.class)
class CleanExpiredTokenTest {
  @Mock private TokenRepository tokenRepository;
  @Mock private JobExecutionContext jobExecutionContext;
  @InjectMocks private CleanExpiredToken cleanExpiredToken;

  @Test
  void shouldDeleteAllExpiredTokens() {
    cleanExpiredToken.execute(jobExecutionContext);
    verify(tokenRepository, times(1)).deleteAllByExpirationDate(any(LocalDateTime.class));
  }
}
