## Summary

Describe what changed and why.

## Type

- [ ] Feature
- [ ] Bug fix
- [ ] Security
- [ ] Database migration
- [ ] Documentation
- [ ] Refactor

## Verification

- [ ] Backend: `mvn clean verify`
- [ ] Flutter: `flutter analyze`
- [ ] Flutter: `flutter test`
- [ ] No secrets or real customer data committed
- [ ] New DB changes use a new Flyway migration
- [ ] API/config contract changes are documented
- [ ] Security/failure cases were considered
- [ ] Backward compatibility was considered

## Security impact

Describe any effect on authentication, OTP, credentials, tokens, device trust,
authorization, PII, Redis state, or signing/encryption material.

## Rollback

Describe how this change can be rolled back safely.
