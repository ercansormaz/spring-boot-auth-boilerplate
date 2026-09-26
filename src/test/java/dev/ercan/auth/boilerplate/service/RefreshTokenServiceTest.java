package dev.ercan.auth.boilerplate.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.ercan.auth.boilerplate.model.entity.Device;
import dev.ercan.auth.boilerplate.model.entity.RefreshToken;
import dev.ercan.auth.boilerplate.repository.RefreshTokenRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Pageable;

class RefreshTokenServiceTest {

  @Test
  void savesRefreshToken() {
    RefreshTokenRepository repository = mock(RefreshTokenRepository.class);
    RefreshTokenService service = new RefreshTokenService(repository);
    RefreshToken token = new RefreshToken();
    when(repository.save(token)).thenReturn(token);

    assertSame(token, service.save(token));
    verify(repository).save(token);
  }

  @Test
  void returnsNullWhenTokenIdDoesNotExist() {
    RefreshTokenRepository repository = mock(RefreshTokenRepository.class);
    RefreshTokenService service = new RefreshTokenService(repository);
    when(repository.findById(9L)).thenReturn(Optional.empty());

    assertNull(service.getById(9L));
  }

  @Test
  void findsRefreshTokenByDevice() {
    RefreshTokenRepository repository = mock(RefreshTokenRepository.class);
    RefreshTokenService service = new RefreshTokenService(repository);
    Device device = new Device();
    RefreshToken token = new RefreshToken();
    when(repository.findByDevice(device)).thenReturn(token);

    assertSame(token, service.getByDevice(device));
  }

  @Test
  void fetchesExpiredTokensUsingFirstPageAndRequestedSize() {
    RefreshTokenRepository repository = mock(RefreshTokenRepository.class);
    RefreshTokenService service = new RefreshTokenService(repository);
    when(repository.findByExpiresAtBefore(
        org.mockito.ArgumentMatchers.any(Instant.class), org.mockito.ArgumentMatchers.any(Pageable.class)))
        .thenReturn(List.of());

    Instant before = Instant.now();
    service.getExpiredTokens(5);
    Instant after = Instant.now();

    ArgumentCaptor<Instant> now = ArgumentCaptor.forClass(Instant.class);
    ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
    verify(repository).findByExpiresAtBefore(now.capture(), pageable.capture());
    assertEquals(0, pageable.getValue().getPageNumber());
    assertEquals(5, pageable.getValue().getPageSize());
    assertFalse(now.getValue().isBefore(before));
    assertFalse(now.getValue().isAfter(after));
  }

  @Test
  void deletesRefreshToken() {
    RefreshTokenRepository repository = mock(RefreshTokenRepository.class);
    RefreshTokenService service = new RefreshTokenService(repository);
    RefreshToken token = new RefreshToken();

    service.delete(token);

    verify(repository).delete(token);
  }
}
