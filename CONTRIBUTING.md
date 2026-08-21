# Contributing

## Branches

Use short-lived feature branches:

```text
feat/<topic>
fix/<topic>
docs/<topic>
```

## Commit style

Recommended conventional commits:

```text
feat(auth): add registration OTP flow
fix(session): revoke refresh family on reuse
docs(api): document new-device challenge
test(auth): cover registration state transitions
```

## Pull request checklist

- [ ] Change has tests where behavior changed
- [ ] `mvn clean verify` passes for backend changes
- [ ] `flutter analyze` and `flutter test` pass for Flutter changes
- [ ] No secret or production credential is committed
- [ ] DB change uses a new Flyway migration
- [ ] API/config schema change is documented
- [ ] Security-impacting changes include threat/failure-case notes
- [ ] Backward compatibility with supported app versions was considered

## Architecture rule

Do not add database-configured arbitrary Java class/method execution.

New dynamic behavior must be exposed through an explicit, allowlisted contract.
