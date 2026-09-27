package com.hdfclife.ledger.service;

import com.hdfclife.ledger.domain.*;
import com.hdfclife.ledger.dto.ClaimResponse;
import com.hdfclife.ledger.dto.CreateClaimRequest;
import com.hdfclife.ledger.exception.ClaimNotFoundException;
import com.hdfclife.ledger.exception.PolicyNotFoundException;
import com.hdfclife.ledger.repo.ClaimRepository;
import com.hdfclife.ledger.repo.PolicyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClaimService {
    private final ClaimRepository claimRepository;
    private final PolicyRepository policyRepository;

    public ClaimService(ClaimRepository claimRepository, PolicyRepository policyRepository) {
        this.claimRepository = claimRepository;
        this.policyRepository = policyRepository;
    }

    @Transactional
    public ClaimResponse create(CreateClaimRequest request) {
        Policy policy = policyRepository.findByPolicyNo(request.policyNo())
                .orElseThrow(() -> new PolicyNotFoundException(request.policyNo()));

        long next = claimRepository.count() + 1;
        String claimNo = "CLM-" + String.format("%02d", next);

        Claim claim = new Claim(
                claimNo,
                policy,
                request.claimAmount(),
                ClaimUrgency.valueOf(request.urgency()),
                ClaimStatus.SUBMITTED
        );

        return toResponse(claimRepository.save(claim));
    }

    @Transactional(readOnly = true)
    public List<ClaimResponse> findByPolicy(String policyNo) {
        if (!policyRepository.existsByPolicyNo(policyNo)) {
            throw new PolicyNotFoundException(policyNo);
        }
        return claimRepository.findByPolicy_PolicyNoOrderByClaimNoAsc(policyNo)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ClaimResponse findByClaimNo(String claimNo) {
        return claimRepository.findByClaimNo(claimNo)
                .map(this::toResponse)
                .orElseThrow(() -> new ClaimNotFoundException(claimNo));
    }

    private ClaimResponse toResponse(Claim claim) {
        return new ClaimResponse(
                claim.getClaimNo(),
                claim.getPolicy().getPolicyNo(),
                claim.getAmount(),
                claim.getUrgency().name(),
                claim.getStatus().name()
        );
    }
}
