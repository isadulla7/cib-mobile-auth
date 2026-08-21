# Local Development

## Requirements

- Java 21
- Maven
- Docker + Docker Compose
- Flutter SDK for the mobile client

## Fastest start

From repository root:

```bash
docker compose up --build
```

Local services:

| Service | Address |
|---|---|
| Auth API | `http://localhost:8080` |
| PostgreSQL | `localhost:5432` |
| Redis | `localhost:6379` |

Health:

```bash
curl http://localhost:8080/actuator/health
```

## Mock contracts

Local-only defaults:

```text
SMS OTP: 111111
SMS provider result: mock-sms-static-response
MyID: verified=true after basic local field validation
JWT key: ephemeral RSA key when development settings allow it
```

Do not copy these behaviors into production.

## Backend without Docker

Start PostgreSQL and Redis, then:

```bash
cd backend
mvn spring-boot:run
```

Run tests:

```bash
mvn clean verify
```

## Flutter

If the platform folders have not yet been generated:

```bash
cd flutter
flutter create .
flutter pub get
```

Android emulator:

```bash
flutter run --dart-define=API_BASE_URL=http://10.0.2.2:8080
```

Physical devices must use a reachable development API address rather than `localhost`.

## Environment

Copy:

```bash
cp .env.example .env
```

Never commit `.env`.

Production values must not use the repository's development defaults.
