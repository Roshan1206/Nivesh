package com.nivesh.library.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * Base class for auditing user
 *
 * @author Roshan
 */
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public abstract class BaseAudit {

    /**
     * Current date time in which the record is created
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * email for which the record is created
     */
    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false)
    private String createdBy;

    /**
     * record's last modified/updated date
     */
    @LastModifiedDate
    @Column(name = "updated_at", insertable = false)
    private Instant updatedAt;

    /**
     * record's last modifier name
     */
    @LastModifiedBy
    @Column(name = "updated_by", insertable = false)
    private String modifiedBy;
}
