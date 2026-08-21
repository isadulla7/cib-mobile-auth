package uz.cib.mobile.auth.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uz.cib.mobile.auth.domain.SessionStatus;
import uz.cib.mobile.auth.repository.AuthSessionRepository;

import java.util.UUID;

@Service
public class SessionRevocationService {
    private final AuthSessionRepository sessions;

    public SessionRevocationService(AuthSessionRepository sessions) {
        this.sessions = sessions;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void revokeFamily(UUID familyId) {
        sessions.findByTokenFamilyIdAndStatus(familyId, SessionStatus.ACTIVE)
                .forEach(session -> session.revoke());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void expireSession(UUID sessionId) {
        sessions.findById(sessionId).ifPresent(session -> {
            if (session.getStatus() == SessionStatus.ACTIVE) session.expire();
        });
    }
}
