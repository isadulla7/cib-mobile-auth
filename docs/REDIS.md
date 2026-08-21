# Redis Design

## Purpose

Redis is used only for state that is temporary, high-churn, TTL-oriented or cache-oriented.

It is not the durable user/account source of truth.

## Keyspace

```text
auth_policy:ACTIVE_VERSION
auth_policy:{version}
auth_flow:{flowId}
otp:registration:{flowId}
otp:login_new_device:{challengeId}
device_challenge:{challengeId}
login_fail:{phoneHash}
rate:login:ip:{ipHash}
refresh_reuse:{refreshHash}
```

## Local vs production

### Local

Single Redis node:

```text
localhost:6379
```

### Production

Redis Cluster via Spring Data Redis + Lettuce.

Recommended operational characteristics:

- TLS
- Redis ACLs
- multi-AZ topology
- replication/failover
- memory and latency alarms
- explicit eviction policy
- capacity headroom
- backup strategy for data that operational policy considers recoverable
- no public network exposure

## Failure semantics

Authentication state must fail safely.

Examples:

- Missing OTP key → challenge invalid/expired
- Redis unavailable during OTP verification → do not silently accept OTP
- Missing registration-flow key → client must restart the flow
- Refresh reuse marker unavailable → security handling must not downgrade into “accept”

## Why OTP is in Redis

OTP data naturally requires:

- short TTL
- attempt counters
- atomic verify/consume
- high write churn

The code uses a hash representation and atomic Redis-side operations rather than persisting the plaintext OTP in PostgreSQL.
