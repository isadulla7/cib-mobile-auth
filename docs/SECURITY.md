# Security Model

This repository is a production-oriented starter. It is not a certification of bank-grade security.

## Implemented security properties

### Credentials

- Password: Argon2id
- PIN: server-side pepper + Argon2id
- OTP: keyed hash representation, short TTL
- Refresh token: cryptographically random opaque value; only a hash is persisted

### Sessions

- Short-lived signed JWT access tokens
- Rotating refresh tokens
- Refresh-token reuse detection
- Session/device binding
- Server-side session revocation

### Registration

- Server-authoritative state machine
- No client-side step skipping
- OTP verification before identity flow
- Identity provider behind an adapter
- Unique-account constraints remain server-side

### Login

- Generic invalid-credential behavior
- Dummy password verification for unknown users
- New-device OTP challenge
- Login-attempt/rate state
- Trusted-device record stored server-side

### Configuration

Configuration may alter policy and UI only inside hard server bounds.

Configuration cannot:

- select arbitrary Spring beans
- execute arbitrary Java methods
- disable signature verification
- change hashing implementation
- bypass authentication/authorization
- expose secret material to Flutter

## Secrets

Never commit production values for:

- `APP_OTP_PEPPER`
- `APP_PIN_PEPPER`
- `APP_DEVICE_PEPPER`
- JWT private key
- database production password
- Redis production password

Production secrets should come from an approved secrets manager/KMS/HSM strategy.

## Before real banking deployment

Required work includes at minimum:

1. Threat model and abuse-case review
2. Penetration testing
3. SAST/SCA/secret-scanning gates
4. Real SMS signing and anti-fraud behavior
5. Real MyID/DID contract implementation
6. Device attestation/integrity solution
7. KMS/HSM-backed key management
8. API gateway/WAF/rate limiting
9. SIEM/security-event integration
10. Incident response and token/key revocation procedures
11. Privacy/data-retention review for PINFL/passport data
12. Load tests for Redis/PostgreSQL/session issuance
13. Disaster-recovery testing
14. Security review of Flutter secure storage and TLS/certificate policy
