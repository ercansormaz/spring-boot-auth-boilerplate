package dev.ercan.auth.boilerplate.model.pojo;

import java.time.Instant;
import java.util.UUID;

public record QrCodeDetail(UUID id, Instant expiresAt, byte[] image, long pollingTimeout) {

}
