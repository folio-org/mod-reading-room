package org.folio.readingroom.repository;

import java.util.UUID;
import org.folio.readingroom.domain.entity.PatronPermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatronPermissionsRepository extends JpaRepository<PatronPermissionEntity, UUID> {
}
