# CIB Mobile Auth — Architecture

## Scope

This repository implements the authentication boundary for the CIB mobile application.

Implemented flows:

- Registration: phone → OTP → PINFL/passport → password → PIN
- Login: phone + password
- New-device challenge: OTP before session issuance
- Access token + rotating opaque refresh token
- Dynamic/versioned auth policy and Flutter UI configuration
- Mock SMS and mock identity provider adapters

## System context

```mermaid
flowchart LR
    U[Mobile user] --> F[Flutter App]
    F -->|HTTPS / JSON| API[Spring Boot Auth API]
    API --> PG[(PostgreSQL)]
    API --> R[(Redis)]
    API --> SMS[SMS Gateway Adapter]
    API --> IDP[Identity Provider Adapter]
```

PostgreSQL is the durable source of truth. Redis stores ephemeral authentication state such as OTP challenges, registration flows, throttling state and cached policy.

## Backend layers

```mermaid
flowchart TD
    C[Controllers] --> O[Registration / Login Orchestrators]
    O --> SM[Server-side Auth State Machine]
    O --> P[Policy Resolver]
    O --> V[Validation Engine]
    V --> VR[Allowlisted Validator Registry]
    O --> OTP[OTP Service]
    O --> CR[Credential Service]
    O --> DT[Device Trust Service]
    O --> SS[Session Service]
    SS --> JWT[JWT Access Token Service]
    SS --> RT[Opaque Refresh Token Service]

    P --> R[(Redis Cache)]
    P --> PG[(PostgreSQL)]
    OTP --> R
    CR --> PG
    DT --> PG
    SS --> PG
```

## Core design rule

The system is dynamic, but configuration cannot execute arbitrary backend code.

Bad:

```text
beanName = userService
methodName = deleteEverything
```

Allowed:

```text
validatorCode = PHONE_FORMAT
policyKey     = OTP_TTL_SECONDS
fieldType     = PHONE
```

Every executable capability is allowlisted in Java.

## Registration state machine

```mermaid
stateDiagram-v2
    [*] --> OTP_SENT: start(phone)
    OTP_SENT --> PHONE_VERIFIED: verify OTP
    PHONE_VERIFIED --> IDENTITY_VERIFIED: verify PINFL/passport
    IDENTITY_VERIFIED --> PASSWORD_SET: set password
    PASSWORD_SET --> COMPLETED: set PIN + create session
    COMPLETED --> [*]
```

The client never owns the authoritative state. A direct call to a later endpoint is rejected if the server-side flow is not in the expected state.

## Login state machine

```mermaid
flowchart LR
    A[Phone + Password] --> B{Credentials valid?}
    B -->|No| X[Generic auth failure]
    B -->|Yes| C{Trusted device?}
    C -->|Yes| D[Issue access + refresh session]
    C -->|No| E[Issue OTP challenge]
    E --> F{OTP valid?}
    F -->|No| G[Throttle / reject]
    F -->|Yes| H[Trust device]
    H --> D
```

## Persistence boundary

### PostgreSQL

Durable state:

- users
- password/PIN hashes
- trusted devices
- refresh sessions
- audit
- versioned auth policy/UI configuration

### Redis

Ephemeral state:

- OTP challenges
- registration flow state
- new-device login challenge state
- login attempt counters
- rate limiting
- policy cache
- refresh-token reuse markers

## Package structure

```text
uz.cib.mobile
├── auth
│   ├── api
│   ├── application
│   ├── domain
│   ├── engine
│   ├── infra
│   ├── integration
│   ├── repository
│   └── security
├── common
└── config
```

## Dynamic vs static boundary

| Area | Dynamic / policy-driven | Static enforcement |
|---|---|---|
| OTP | length, TTL, resend delay, max attempts | secure generation, hashing, atomic consume |
| Password | minimum requirements | Argon2id hashing/verification |
| PIN | length/policy | pepper + Argon2id |
| Login | enabled methods, risk thresholds | authentication enforcement |
| UI | field order, labels, min app version | supported widget contract |
| Session | duration policy | refresh rotation and reuse detection |
| Device | trust duration | device binding verification |
| JWT | issuer/audience metadata | signing and signature verification |

## Deployment shape

Local development uses one PostgreSQL container and one Redis node.

Production target:

```mermaid
flowchart TB
    LB[Load Balancer] --> A1[Auth API instance 1]
    LB --> A2[Auth API instance 2]
    LB --> AN[Auth API instance N]
    A1 --> PG[(PostgreSQL HA)]
    A2 --> PG
    AN --> PG
    A1 --> RC[(Redis Cluster)]
    A2 --> RC
    AN --> RC
```

The API instances are stateless except for durable/ephemeral state in PostgreSQL and Redis.
