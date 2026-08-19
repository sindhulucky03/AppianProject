package com.schwab.auditlog.persistence.repository;

import com.schwab.auditlog.persistence.entity.AuditChainStateEntity;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditChainStateRepository extends JpaRepository<AuditChainStateEntity, Short> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select chainState from AuditChainStateEntity chainState where chainState.chainId = :chainId")
    Optional<AuditChainStateEntity> findByChainIdForUpdate(@Param("chainId") short chainId);
}
