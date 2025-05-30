package it.theapplegeek.spring_starter_pack.security.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import it.theapplegeek.spring_starter_pack.user.model.User;
import it.theapplegeek.spring_starter_pack.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class LoadUserFromUsernameServiceTest {
  @Mock private UserRepository userRepository;
  @InjectMocks private LoadUserFromUsernameService loadUserFromUsernameService;

  @Test
  void shouldLoadUserFromUsername() {
    // Given
    User user = mock(User.class);
    given(userRepository.findByUsername("admin")).willReturn(Optional.ofNullable(user));

    // When
    // Then
    assertThat(loadUserFromUsernameService.loadUserFromUsername("admin")).isEqualTo(user);
  }

  @Test
  void shouldNotLoadUserFromUsername() {
    // Given
    given(userRepository.findByUsername("admin")).willReturn(Optional.empty());

    // When
    // Then
    assertThrows(
        UsernameNotFoundException.class,
        () -> loadUserFromUsernameService.loadUserFromUsername("admin"));
  }
}
