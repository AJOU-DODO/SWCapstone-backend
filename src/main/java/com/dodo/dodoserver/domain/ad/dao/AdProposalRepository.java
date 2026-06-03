package com.dodo.dodoserver.domain.ad.dao;

import com.dodo.dodoserver.domain.ad.entity.AdProposal;
import com.dodo.dodoserver.domain.ad.entity.AdProposalStatus;
import com.dodo.dodoserver.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdProposalRepository extends JpaRepository<AdProposal, Long> {
    List<AdProposal> findAllByStatus(AdProposalStatus status);
    List<AdProposal> findAllByAdvertiser(User advertiser);
    long countByAdvertiserAndStatus(User advertiser, AdProposalStatus status);
}
