package uz.cib.mobile.auth.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uz.cib.mobile.auth.domain.AuthAuditEntity;
import uz.cib.mobile.auth.repository.AuthAuditRepository;
import uz.cib.mobile.common.HashingSupport;

import java.util.Map;
import java.util.UUID;

@Service
public class AuditService {
    private final AuthAuditRepository repository;
    private final ObjectMapper mapper;

    public AuditService(AuthAuditRepository repository, ObjectMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(UUID userId, String event, String result, String correlationId,
                       String deviceHash, String remoteIp, Map<String, Object> details) {
        repository.save(AuthAuditEntity.create(
                userId, event, result, correlationId, deviceHash,
                remoteIp == null ? null : HashingSupport.sha256Hex(remoteIp),
                mapper.valueToTree(details == null ? Map.of() : details)
        ));
    }
}
