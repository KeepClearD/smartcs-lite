package com.smartcs.lite.repository;

import com.smartcs.lite.model.entity.KnowledgeBase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KnowledgeBaseRepository extends JpaRepository<KnowledgeBase, Long> {
    List<KnowledgeBase> findByTenantId(Long tenantId);
}
