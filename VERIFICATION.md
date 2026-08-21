# Verification Report

## Completed static checks

- Java source files: 87
- Dart source files: 10
- Flyway migrations: 2
- `pom.xml`: valid XML
- YAML files: parse successfully
- Internal `uz.cib.mobile.*` Java imports: resolved against generated source tree
- Java delimiter balance: checked across all main Java files
- JDK-only Java 21 subset: compiled successfully with `javac --release 21`
- Dart SDK constraint aligned to Dart 3.8+ for the selected Flutter packages

## Environment limitation

This execution environment contains Java 21, but does **not** contain Maven,
Flutter SDK, or Docker. Therefore a full dependency-aware `mvn test/package`,
`flutter analyze/test`, and `docker compose up` could not be executed here.

Run these in a development/CI environment:

```bash
cd backend
mvn clean verify

cd ../flutter
flutter create .
flutter pub get
flutter analyze
flutter test

cd ..
docker compose up --build
```

The generated code is a production-oriented starter, not a substitute for a
bank's security review, penetration testing, threat modeling, KMS/HSM integration,
real SMS/MyID contract testing, or operational hardening.
