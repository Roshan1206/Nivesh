package com.nivesh.customer.entity;

import com.nivesh.library.entity.BaseAudit;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * doc_id, customer_id, doc_type(),
 * doc_number_encrypted, file_path(S3),
 * verified_at, verified_by, expiry_date,
 * status
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "kyc_documents")
public class KycDocument extends BaseAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "document_id")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "document_type", nullable = false)
    private DocumentType type;

    @Column(name = "document_number", nullable = false, unique = true)
    private String documentNumber;

    @Column(name = "file_path")
    private String filePath;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "verified_by", columnDefinition = "kyc_verification_enum")
    private KycVerification verifiedBy;

    @Column(name = "emp_no")
    private String empNo;

    public KycDocument(Customer customer, DocumentType type, String documentNumber) {
        this.customer = customer;
        this.type = type;
        this.documentNumber = documentNumber;
    }

    public enum KycVerification {

        SYSTEM_UIDAI,
        SYSTEM_NSDL,
        SYSTEM_VIDEO_KYC,
        SYSTEM_DIGI_LOCKER,
        EMPLOYEE
    }

    public enum DocumentType {

        PAN_CARD,
        AADHAAR_CARD,
        PASSPORT,
        VOTER_ID
    }
}
