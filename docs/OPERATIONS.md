# Operations & Deployment Notes

## Runtime components

```text
Flutter mobile app
      ↓
API gateway / load balancer
      ↓
Spring Boot auth instances
      ↓                 ↓
PostgreSQL HA       Redis Cluster
      ↓
Audit / monitoring / SIEM
```

## Health and observability

Spring Boot Actuator exposes configured health/info/metrics endpoints.

Production should add:

- Prometheus-compatible metrics export or organization standard
- distributed tracing
- structured JSON logs
- correlation/request IDs
- authentication-security counters
- Redis latency/eviction dashboards
- PostgreSQL pool/slow-query dashboards
- alerting on refresh-token reuse and unusual login failures

## Suggested SLO-sensitive metrics

- login success/failure rate
- registration completion rate
- OTP issue latency
- OTP verification latency
- external identity-provider latency/error rate
- session-issuance latency
- refresh success/reuse rate
- Redis command latency
- PostgreSQL pool saturation

## Production configuration

Use Spring profile:

```text
SPRING_PROFILES_ACTIVE=cluster
```

Redis cluster environment:

```text
REDIS_NODE_1=host1:6379
REDIS_NODE_2=host2:6379
REDIS_NODE_3=host3:6379
REDIS_PASSWORD=...
```

JWT production keys must be provisioned outside Git and injected at runtime.

## Rollout

Prefer backward-compatible rolling deployment:

1. Apply compatible DB migration.
2. Deploy backend version.
3. Confirm health and metrics.
4. Publish new auth/UI policy only after the backend version supports it.
5. Roll Flutter clients gradually.
6. Retain support for older supported schema versions until minimum app version is raised.
