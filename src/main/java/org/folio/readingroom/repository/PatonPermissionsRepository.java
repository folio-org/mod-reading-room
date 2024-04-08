package org.folio.readingroom.repository;

import org.folio.readingroom.domain.entity.PatronPermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PatonPermissionsRepository extends JpaRepository<PatronPermissionEntity, UUID> {
}
