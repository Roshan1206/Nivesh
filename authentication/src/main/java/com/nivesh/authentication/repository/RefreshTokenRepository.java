package com.nivesh.authentication.repository;

import com.nivesh.authentication.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    @Modifying
    @Query("""
        UPDATE RefreshToken rt SET rt.revoked = true, rt.revokedReason = :reason, rt.revokedAt = CURRENT_TIMESTAMP
        WHERE rt.user.id = :userId AND rt.revoked = false AND rt.tokenId = :tokenId
               \s""")
    void revokeUser(@Param("userId") UUID userId, @Param("reason") String reason, @Param("tokenId") UUID tokenId);

    @Modifying
    @Query("""
        UPDATE RefreshToken rt SET rt.revoked = true, rt.revokedAt = CURRENT_TIMESTAMP, rt.revokedReason = :reason
        WHERE rt.user.id = :userId AND rt.revoked = false
    """)
    void revokeAllByUser(@Param("userId") UUID userId, @Param("reason") String reason);

    Optional<RefreshToken> findByTokenId(UUID tokenId);
}
