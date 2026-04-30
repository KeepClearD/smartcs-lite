package com.smartcs.lite.repository;

import com.smartcs.lite.model.entity.Faq;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FaqRepository extends JpaRepository<Faq, Long> {

    List<Faq> findByKbIdAndTenantId(Long kbId, Long tenantId);

    List<Faq> findByTenantIdAndStatus(Long tenantId, String status);
}
