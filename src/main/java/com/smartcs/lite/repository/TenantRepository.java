// src/main/java/com/smartcs/lite/repository/TenantRepository.java
package com.smartcs.lite.repository;

import com.smartcs.lite.model.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantRepository extends JpaRepository<Tenant, Long> {
}
