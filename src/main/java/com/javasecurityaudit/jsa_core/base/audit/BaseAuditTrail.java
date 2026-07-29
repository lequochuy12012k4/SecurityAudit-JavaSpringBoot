package com.javasecurityaudit.jsa_core.base.audit;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class BaseAuditTrail {

    @CreatedBy
    @Column(name = "created_by", updatable = false, length = 100)
    String createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;

    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    String updatedBy;

    @LastModifiedDate
    @Column(name = "updated_at")
    LocalDateTime updatedAt;
}