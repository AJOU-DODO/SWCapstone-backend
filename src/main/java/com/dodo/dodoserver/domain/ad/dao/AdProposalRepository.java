package com.dodo.dodoserver.domain.ad.dao;

import com.dodo.dodoserver.domain.ad.entity.AdProposal;
import com.dodo.dodoserver.domain.ad.entity.AdProposalStatus;
import com.dodo.dodoserver.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AdProposalRepository extends JpaRepository<AdProposal, Long> {
    List<AdProposal> findAllByStatus(AdProposalStatus status);
    List<AdProposal> findAllByAdvertiser(User advertiser);
    long countByAdvertiserAndStatus(User advertiser, AdProposalStatus status);

    @Query("SELECT p.advertiser.id, COUNT(p) FROM AdProposal p WHERE p.advertiser IN :advertisers AND p.status = :status GROUP BY p.advertiser.id")
    List<Object[]> countPendingProposalsByAdvertisers(@Param("advertisers") List<User> advertisers, @Param("status") AdProposalStatus status);
}
