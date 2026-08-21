# ADR-001 — Dynamic Auth Boundary

**Status:** Accepted

## Context

The CIB mobile application requires backend-driven UI and dynamic authentication policy, but the authentication layer must not allow database configuration to become arbitrary executable code.

## Decision

Use versioned configuration for:

- field definitions
- labels
- enabled login methods
- OTP/password/session policy
- validator codes
- app-version/capability requirements

Executable validators/actions are resolved only from an allowlisted Java registry.

Security mechanisms remain code-owned.

## Consequences

### Positive

- product/auth UI can evolve without a Flutter release for supported components
- configuration can be audited/versioned
- arbitrary Reflection-based execution is removed
- security invariants remain testable in code

### Negative

- a new UI capability still requires Flutter support
- a new executable validation capability still requires backend code
- configuration publication requires schema/semantic validation
