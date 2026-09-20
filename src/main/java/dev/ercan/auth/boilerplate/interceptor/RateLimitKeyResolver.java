package dev.ercan.auth.boilerplate.interceptor;

import dev.ercan.auth.boilerplate.annotation.RateLimit;
import dev.ercan.auth.boilerplate.config.RateLimitProperties;
import dev.ercan.auth.boilerplate.model.entity.Account;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import java.net.InetAddress;
import java.net.UnknownHostException;

@RequiredArgsConstructor
final class RateLimitKeyResolver {

  private static final String GLOBAL_SCOPE_KEY = "global";
  private static final String KEY_PREFIX = "rl";

  private final RateLimitProperties properties;

  String resolve(HttpServletRequest request, RateLimit annotation) {
    String scopeKey = switch (annotation.scope()) {
      case IP -> resolveClientIp(request);
      case USER -> resolveUserId();
      case GLOBAL -> GLOBAL_SCOPE_KEY;
    };

    return "%s:%s:%s".formatted(KEY_PREFIX, annotation.type(), scopeKey);
  }

  private String resolveUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !(authentication.getPrincipal() instanceof Account account)
        || account.getId() == null) {
      throw new IllegalStateException("User scope rate limit failed: No authenticated user");
    }

    return String.valueOf(account.getId());
  }

  private String resolveClientIp(HttpServletRequest request) {
    String remoteAddress = request.getRemoteAddr();

    if (!StringUtils.hasText(remoteAddress)) {
      throw new IllegalStateException("IP scope rate limit failed: Unable to determine remote address");
    }

    if (!isTrustedProxy(remoteAddress)) {
      return remoteAddress;
    }

    String forwardedFor = request.getHeader(properties.getIpHeaderName());
    if (!StringUtils.hasText(forwardedFor)) {
      return remoteAddress;
    }

    String[] addresses = forwardedFor.split(",");
    for (int index = addresses.length - 1; index >= 0; index--) {
      InetAddress address = parseAddress(addresses[index].trim());
      if (!isTrustedProxy(address)) {
        return address.getHostAddress();
      }
    }

    return parseAddress(addresses[0].trim()).getHostAddress();
  }

  private boolean isTrustedProxy(String address) {
    return isTrustedProxy(parseAddress(address));
  }

  private boolean isTrustedProxy(InetAddress address) {
    return properties.getTrustedProxies().stream().map(this::parseNetwork)
        .anyMatch(network -> network.contains(address));
  }

  private InetAddress parseAddress(String address) {
    if (!StringUtils.hasText(address)) {
      throw new IllegalStateException("Forwarded IP is empty");
    }

    try {
      return InetAddress.getByName(address);
    } catch (UnknownHostException e) {
      throw new IllegalStateException("Forwarded IP is invalid: " + address, e);
    }
  }

  private TrustedNetwork parseNetwork(String value) {
    if (!StringUtils.hasText(value)) {
      throw new IllegalStateException("Trusted proxy network is empty");
    }

    String[] parts = value.trim().split("/", 2);
    InetAddress networkAddress = parseAddress(parts[0]);
    int defaultPrefixLength = networkAddress.getAddress().length * Byte.SIZE;
    int prefixLength = parts.length == 1 ? defaultPrefixLength : parsePrefixLength(parts[1], defaultPrefixLength);

    return new TrustedNetwork(networkAddress, prefixLength);
  }

  private int parsePrefixLength(String value, int maxPrefixLength) {
    try {
      int prefixLength = Integer.parseInt(value);
      if (prefixLength < 0 || prefixLength > maxPrefixLength) {
        throw new IllegalStateException("Trusted proxy prefix length is invalid: " + value);
      }
      return prefixLength;
    } catch (NumberFormatException e) {
      throw new IllegalStateException("Trusted proxy prefix length is invalid: " + value, e);
    }
  }

  private record TrustedNetwork(InetAddress address, int prefixLength) {

    private boolean contains(InetAddress candidate) {
      byte[] networkBytes = address.getAddress();
      byte[] candidateBytes = candidate.getAddress();

      if (networkBytes.length != candidateBytes.length) {
        return false;
      }

      int fullBytes = prefixLength / Byte.SIZE;
      int remainingBits = prefixLength % Byte.SIZE;

      for (int index = 0; index < fullBytes; index++) {
        if (networkBytes[index] != candidateBytes[index]) {
          return false;
        }
      }

      if (remainingBits == 0) {
        return true;
      }

      int mask = 0xFF << (Byte.SIZE - remainingBits);
      return (networkBytes[fullBytes] & mask) == (candidateBytes[fullBytes] & mask);
    }
  }
}
