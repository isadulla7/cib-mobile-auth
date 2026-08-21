# Security Policy

This repository contains authentication code. Treat all auth/security changes as security-sensitive.

## Do not commit

- production credentials
- private signing keys
- OTP/PIN/password values
- `.env`
- production database dumps
- real customer PINFL/passport data

## Reporting

Use the organization's private security-reporting process rather than opening a public issue for a vulnerability.

## Review requirements

Changes to credential hashing, JWT validation, refresh-token rotation, OTP verification, device trust or authorization should receive dedicated security review before production deployment.

See [`docs/SECURITY.md`](docs/SECURITY.md) for the technical security model.
