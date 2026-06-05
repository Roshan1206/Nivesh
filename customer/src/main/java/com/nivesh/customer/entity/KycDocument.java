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

    /** Unique identifier for this record. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "document_id")
    private UUID id;

    /** Customer that owns this record. */
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    /** Type represented by this record. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "document_type", nullable = false)
    private DocumentType type;

    /** Identifier printed on the submitted KYC document. */
    @Column(name = "document_number", nullable = false, unique = true)
    private String documentNumber;

    /** Storage path for the uploaded KYC document. */
    @Column(name = "file_path")
    private String filePath;

    /** Verification strategy used for this KYC document. */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "verified_by", columnDefinition = "kyc_verification_enum")
    private KycVerification verifiedBy;

    /** Employee number of the verifier, when manually reviewed. */
    @Column(name = "emp_no")
    private String empNo;

    /**
     * Creates a KYC document for the given customer and uploaded document details.
     */
    public KycDocument(Customer customer, DocumentType type, String documentNumber) {
        this.customer = customer;
        this.type = type;
        this.documentNumber = documentNumber;
    }

    public enum KycVerification {

        /** Verification completed through UIDAI integration. */
        SYSTEM_UIDAI,

        /** Verification completed through NSDL integration. */
        SYSTEM_NSDL,

        /** Verification completed through video KYC. */
        SYSTEM_VIDEO_KYC,

        /** Verification completed through DigiLocker. */
        SYSTEM_DIGI_LOCKER,

        /** Verification completed by an employee reviewer. */
        EMPLOYEE
    }

    public enum DocumentType {

        /** Permanent Account Number card document. */
        PAN_CARD,

        /** Aadhaar identity card document. */
        AADHAAR_CARD,

        /** Passport identity document. */
        PASSPORT,

        /** Voter identity document. */
        VOTER_ID
    }
}
