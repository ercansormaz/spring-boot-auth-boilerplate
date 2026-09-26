package dev.ercan.auth.boilerplate.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.ercan.auth.boilerplate.model.entity.AccessToken;
import dev.ercan.auth.boilerplate.model.entity.Device;
import dev.ercan.auth.boilerplate.repository.AccessTokenRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Pageable;
import org.springframework.dao.DataAccessResourceFailureException;

class AccessTokenServiceTest {

  @Test
  void savesAccessTokenThroughRepository() {
    AccessTokenRepository repository = mock(AccessTokenRepository.class);
    AccessTokenService service = new AccessTokenService(repository);
    AccessToken token = new AccessToken();
    when(repository.save(token)).thenReturn(token);

    assertSame(token, service.save(token));

    verify(repository).save(token);
  }

  @Test
  void returnsTokenWhenIdExists() {
    AccessTokenRepository repository = mock(AccessTokenRepository.class);
    AccessTokenService service = new AccessTokenService(repository);
    AccessToken token = new AccessToken();
    when(repository.findById(10L)).thenReturn(Optional.of(token));

    assertSame(token, service.getById(10L));
  }

  @Test
  void returnsNullWhenTokenIdIsNotFound() {
    AccessTokenRepository repository = mock(AccessTokenRepository.class);
    AccessTokenService service = new AccessTokenService(repository);
    when(repository.findById(10L)).thenReturn(Optional.empty());

    assertNull(service.getById(10L));
  }

  @Test
  void fetchesExpiredTokensOnFirstPageWithRequestedSize() {
    AccessTokenRepository repository = mock(AccessTokenRepository.class);
    AccessTokenService service = new AccessTokenService(repository);
    when(repository.findByExpiresAtBefore(
        org.mockito.ArgumentMatchers.any(Instant.class), org.mockito.ArgumentMatchers.any(Pageable.class)))
        .thenReturn(List.of());

    Instant before = Instant.now();
    service.getExpiredTokens(7);
    Instant after = Instant.now();

    ArgumentCaptor<Instant> now = ArgumentCaptor.forClass(Instant.class);
    ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
    verify(repository).findByExpiresAtBefore(now.capture(), pageable.capture());
    assertEquals(0, pageable.getValue().getPageNumber());
    assertEquals(7, pageable.getValue().getPageSize());
    assertFalse(now.getValue().isBefore(before));
    assertFalse(now.getValue().isAfter(after));
  }

  @Test
  void findsTokenByDevice() {
    AccessTokenRepository repository = mock(AccessTokenRepository.class);
    AccessTokenService service = new AccessTokenService(repository);
    Device device = new Device();
    AccessToken token = new AccessToken();
    when(repository.findByDevice(device)).thenReturn(token);

    assertSame(token, service.getByDevice(device));
  }

  @Test
  void deletesAccessTokenThroughRepository() {
    AccessTokenRepository repository = mock(AccessTokenRepository.class);
    AccessTokenService service = new AccessTokenService(repository);
    AccessToken token = new AccessToken();

    service.delete(token);

    verify(repository).delete(token);
  }

  @Test
  void propagatesRepositoryFailureWhenFetchingExpiredTokens() {
    AccessTokenRepository repository = mock(AccessTokenRepository.class);
    AccessTokenService service = new AccessTokenService(repository);
    DataAccessResourceFailureException failure = new DataAccessResourceFailureException("database unavailable");
    when(repository.findByExpiresAtBefore(
        org.mockito.ArgumentMatchers.any(Instant.class), org.mockito.ArgumentMatchers.any(Pageable.class)))
        .thenThrow(failure);

    assertSame(failure, assertThrows(DataAccessResourceFailureException.class, () -> service.getExpiredTokens(3)));
  }
}
