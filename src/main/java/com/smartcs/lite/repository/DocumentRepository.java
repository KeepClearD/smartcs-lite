package com.smartcs.lite.repository;

import com.smartcs.lite.model.entity.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<DocumentEntity, Long> {
    List<DocumentEntity> findByKbIdAndTenantId(Long kbId, Long tenantId);
}
