# Spring Boot Auth Boilerplate

A quick-start authentication backend for projects with web and mobile clients. It provides common authentication and session-management flows so you can spend less time wiring up a backend foundation and more time building your product.

This repository contains the backend REST API; it does not include web or mobile client applications. Use it as a starting point and adapt its integrations, policies, and configuration to your project.

> [!IMPORTANT]
> **Single-node deployment only.** The current implementation is supported on a single application instance. Caffeine caches and rate-limit buckets are stored in process memory and are not shared between instances. Running multiple instances behind a load balancer can therefore lead to inconsistent rate limiting and cache behavior. A shared MySQL database does not remove this limitation. Redis-based multi-node support is future work and is not implemented yet.

## Features

- Anonymous authentication
- Email one-time-password (OTP) authentication
- Google and Apple ID-token authentication
- Access and refresh tokens, with Fernet or JWT token formats
- Device tracking and active-session listing
- Session revocation for the current device, other devices, or all devices
- Login history
- Configurable authentication rate limits
- Bruno collection with example requests

## Technology

- Java 21
- Spring Boot 4.1.1
- Maven
- MySQL

## Quick start

### Prerequisites

- JDK 21
- Maven
- A reachable MySQL server and a database for the application

Create the database if it does not already exist:

```sql
CREATE DATABASE boilerplate;
```

### Configure and run

The application reads its database connection and secrets from environment variables. The default configuration uses Fernet tokens and enables both Google and Apple authentication. Set up their client IDs, or disable the providers you do not plan to use in `src/main/resources/application.yml`.

| Environment variable | Purpose | Default |
| --- | --- | --- |
| `MYSQL_HOST` | MySQL host | `localhost` |
| `MYSQL_PORT` | MySQL port | `3306` |
| `MYSQL_DATABASE` | Database name | `boilerplate` |
| `MYSQL_USERNAME` | Database user | `root` |
| `MYSQL_PASSWORD` | Database password | Required |
| `AUTH_TOKEN_FERNET_SECRET` | Fernet token secret (the default token type) | Required |
| `OTP_ENCRYPTION_KEY` | Secret used to sign OTP data | Required |
| `AUTH_GOOGLE_CLIENT_ID` | Google OAuth client ID; required while Google auth is enabled | Required when enabled |
| `AUTH_APPLE_CLIENT_ID` | Apple client ID; required while Apple auth is enabled | Required when enabled |
| `AUTH_TOKEN_JWT_SECRET` | JWT signing secret, if JWT is selected | Required for JWT |
| `AUTH_TOKEN_JWT_ISSUER` | JWT issuer, if JWT is selected | Required for JWT |

Set the required values in your local environment before starting the application. Replace the example values with your own; the Fernet value must be a valid Fernet key.

```bash
export MYSQL_PASSWORD='your-mysql-password'
export AUTH_TOKEN_FERNET_SECRET='your-valid-fernet-key'
export OTP_ENCRYPTION_KEY='your-private-otp-key'
export AUTH_GOOGLE_CLIENT_ID='your-google-client-id'
export AUTH_APPLE_CLIENT_ID='your-apple-client-id'
```

If you are not configuring Google or Apple authentication, set the corresponding `auth.google.enabled` or `auth.apple.enabled` property to `false` in `application.yml`. Do not commit secrets. If you switch `auth.token.type` to `jwt` in `application.yml`, configure the JWT variables instead of the Fernet secret.

The default email sender is `console`: it writes OTP details to the application log instead of sending email. This is useful for local testing only. Implement and configure an `EmailSender` integration before relying on email delivery.

Start the application from the repository root:

```bash
mvn spring-boot:run
```

By default, the API listens on `http://localhost:8080`. The current JPA configuration uses `ddl-auto: update` to update the schema on startup; review and replace this behavior with an appropriate schema-migration strategy for your deployment.

## Authentication API

All authentication requests that create or refresh a session include device metadata. Successful authentication returns an access token, a refresh token, the token type, and the access-token expiry.

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `POST` | `/v1/auth/anonymous` | Create an anonymous account/session |
| `PUT` | `/v1/auth/email` | Request an email OTP |
| `POST` | `/v1/auth/email` | Verify an OTP and authenticate |
| `POST` | `/v1/auth/google` | Authenticate with a Google ID token |
| `POST` | `/v1/auth/apple` | Authenticate with an Apple ID token |
| `POST` | `/v1/auth/refresh` | Refresh tokens |
| `DELETE` | `/v1/auth/logout` | Revoke the current device session |
| `GET` | `/v1/auth/sessions` | List active sessions |
| `DELETE` | `/v1/auth/sessions/{deviceId}` | Revoke a device session |
| `DELETE` | `/v1/auth/sessions/others` | Revoke other sessions |
| `DELETE` | `/v1/auth/sessions` | Revoke all sessions |

Protected endpoints expect the access token in the `Authorization` header:

```http
Authorization: Bearer <access-token>
```

### Example: anonymous authentication

```bash
curl --request POST 'http://localhost:8080/v1/auth/anonymous' \
  --header 'Content-Type: application/json' \
  --data '{
    "remember": true,
    "device": {
      "identifier": "ae2bdbcc-367e-4518-b1cd-9b422681409a",
      "platform": "IOS",
      "type": "PHONE",
      "model": "iPhone",
      "language": "en",
      "app_version": "1.0.0",
      "os_version": "17.0"
    }
  }'
```

### Example: email OTP flow

Request an OTP:

```bash
curl --request PUT 'http://localhost:8080/v1/auth/email' \
  --header 'Content-Type: application/json' \
  --data '{"email":"you@example.com"}'
```

The response contains an OTP ID and signature. With the default console sender, find the OTP value in the application log. Submit the returned ID and signature together with that value and the device information:

```json
{
  "email": "you@example.com",
  "remember": true,
  "otp": {
    "id": "<otp-id-from-response>",
    "value": "<otp-from-application-log>",
    "signature": "<signature-from-response>"
  },
  "device": {
    "identifier": "ae2bdbcc-367e-4518-b1cd-9b422681409a",
    "platform": "IOS",
    "type": "PHONE",
    "model": "iPhone",
    "language": "en",
    "app_version": "1.0.0",
    "os_version": "17.0"
  }
}
```

Send this body as JSON in a `POST` request to `/v1/auth/email`. Google and Apple authentication similarly accept an `id_token` and device details; the token must be issued for the configured client.

## Bruno collection

Import or open the collection in `docs/bruno` with [Bruno](https://www.usebruno.com/). Select the `[LOCAL]` environment to use `http://localhost:8080`. The collection includes example requests for the authentication, logout, and session endpoints, and scripts that carry tokens between requests.

## Tests

Run the unit test suite with:

```bash
mvn test
```

## Extending the starter

- Replace the console email sender with an implementation of `EmailSender` and configure it with `email.sender-type`.
- Configure token type and lifetimes, provider client IDs, OTP behavior, and rate-limit policies in `src/main/resources/application.yml`.
- Adapt the account, device, and session behavior to your application's requirements.
- Before deployment, review secret management, email delivery, database schema management, proxy trust settings, and the single-node limitation.
