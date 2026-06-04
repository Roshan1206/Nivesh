package com.nivesh.transaction.entity;

import com.nivesh.transaction.entity.enums.DrCr;
import com.nivesh.transaction.entity.enums.GlAccountType;
import com.nivesh.transaction.entity.enums.GlCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "gl_accounts")
@Entity
public class GLAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "gl_account_id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "gl_code", nullable = false, unique = true, length = 30)
    private String glCode;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "category", nullable = false, columnDefinition = "gl_category")
    private GlCategory category;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "account_type", nullable = false, columnDefinition = "gl_account_type")
    private GlAccountType accountType;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "normal_balance", nullable = false, columnDefinition = "dr_cr")
    private DrCr normalBalance;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;
}
