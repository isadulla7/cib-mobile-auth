# ADR-002 — PostgreSQL vs Redis Storage Boundary

**Status:** Accepted

## Decision

PostgreSQL stores durable account/session/configuration records.

Redis stores ephemeral authentication workflow state, OTP challenges, counters and caches.

## Rationale

OTP and flow state require TTL and atomic high-churn operations. User identity, credentials, trusted-device records, sessions and audit require durable relational integrity.

## Rule

Redis loss must never create an authentication bypass. Missing ephemeral state invalidates/restarts the affected challenge instead.
