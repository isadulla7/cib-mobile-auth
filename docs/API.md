# Authentication API

Base prefix: `/api/v1`

## Public endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/auth/config` | Read active dynamic auth/UI configuration |
| POST | `/auth/registration/start` | Start registration and send OTP |
| POST | `/auth/registration/{flowId}/otp/verify` | Verify registration OTP |
| POST | `/auth/registration/{flowId}/identity` | Verify PINFL + passport |
| POST | `/auth/registration/{flowId}/password` | Set password |
| POST | `/auth/registration/{flowId}/pin` | Set PIN, complete registration and create session |
| POST | `/auth/login` | Login with phone + password |
| POST | `/auth/login/challenge/{challengeId}/otp` | Verify OTP for a new device |
| POST | `/auth/token/refresh` | Rotate refresh token and issue new access token |
| POST | `/auth/token/logout` | Revoke current session |

## Protected endpoint

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/me` | Validate bearer token and return authenticated identity |

## Device header

Device-aware flows use:

```http
X-Device-Id: <app-generated-random-device-id>
```

The backend does not persist the raw device ID. It stores a server-keyed HMAC representation.

## Registration example

### Start

```http
POST /api/v1/auth/registration/start
Content-Type: application/json

{
  "phone": "+998901234567",
  "policyVersion": 1
}
```

### Verify OTP

```http
POST /api/v1/auth/registration/{flowId}/otp/verify
Content-Type: application/json

{
  "otp": "111111"
}
```

The `111111` value is local/mock behavior only.

### Identity

```http
POST /api/v1/auth/registration/{flowId}/identity
Content-Type: application/json

{
  "pinfl": "12345678901234",
  "passport": "AA1234567"
}
```

### Password

```http
POST /api/v1/auth/registration/{flowId}/password
Content-Type: application/json

{
  "password": "StrongPass1!"
}
```

### PIN / complete

```http
POST /api/v1/auth/registration/{flowId}/pin
X-Device-Id: 8ae5a118-11d0-4e28-94fb-a056d71f365c
Content-Type: application/json

{
  "pin": "1234"
}
```

Successful completion returns an access token, rotating refresh token, token lifetimes and session ID.

## Login

```http
POST /api/v1/auth/login
X-Device-Id: 8ae5a118-11d0-4e28-94fb-a056d71f365c
Content-Type: application/json

{
  "phone": "+998901234567",
  "password": "StrongPass1!"
}
```

A trusted device receives a session directly. A new device receives `OTP_REQUIRED` and a `challengeId`.

## Refresh-token contract

A refresh token is single-use from the client's point of view.

```text
refresh token A
      ↓
POST /auth/token/refresh
      ↓
access token B + refresh token B
```

The client must replace token A immediately. Reusing a consumed refresh token is treated as a security event.

## Error behavior

Authentication errors are deliberately generic where revealing account existence or internal state would create user-enumeration risk.

The client should use machine-readable error codes rather than parsing human-readable messages.
