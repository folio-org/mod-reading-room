package org.folio.readingroom.repository;

import java.util.UUID;
import org.folio.readingroom.domain.entity.AccessLogEntity;
import org.folio.spring.cql.JpaCqlRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccessLogRepository extends JpaCqlRepository<AccessLogEntity, UUID> {
}
