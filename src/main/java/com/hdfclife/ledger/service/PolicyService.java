package com.hdfclife.ledger.service;

import com.hdfclife.ledger.domain.*;
import com.hdfclife.ledger.dto.CreatePolicyRequest;
import com.hdfclife.ledger.dto.PolicyResponse;
import com.hdfclife.ledger.exception.DuplicatePolicyException;
import com.hdfclife.ledger.exception.InvalidRequestException;
import com.hdfclife.ledger.exception.PolicyNotFoundException;
import com.hdfclife.ledger.repo.CustomerRepository;
import com.hdfclife.ledger.repo.PolicyRepository;
import com.hdfclife.ledger.repo.RiderRepository;
import com.hdfclife.ledger.validation.PolicyType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class PolicyService {
    private final PolicyRepository policyRepository;
    private final CustomerRepository customerRepository;
    private final RiderRepository riderRepository;

    public PolicyService(PolicyRepository policyRepository,
                         CustomerRepository customerRepository,
                         RiderRepository riderRepository) {
        this.policyRepository = policyRepository;
        this.customerRepository = customerRepository;
        this.riderRepository = riderRepository;
    }

    @Transactional(readOnly = true)
    public List<PolicyResponse> findAll() {
        return policyRepository.findAllByOrderByPolicyNoAsc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PolicyResponse findByPolicyNo(String policyNo) {
        return toResponse(getPolicy(policyNo));
    }

    @Transactional(readOnly = true)
    public List<PolicyResponse> findByStatus(PolicyStatus status) {
        validatePolicyStatus(status);
        return policyRepository.findByStatusOrderByPolicyNoAsc(status).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<PolicyResponse> findByType(ProductType type) {
        validateProductType(type);
        return policyRepository.findByProductTypeOrderByPolicyNoAsc(type).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<PolicyResponse> findByCustomer(String customer) {
        return policyRepository.findByCustomer_FullNameOrderByPolicyNoAsc(customer).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<PolicyResponse> searchByPremium(Integer minPremium) {
        if (minPremium == null || minPremium < 0) {
            throw new InvalidRequestException("minPremium is required and must be >= 0");
        }
        return policyRepository.findWithPremiumAtLeast(minPremium).stream().map(this::toResponse).toList();
    }

    @Transactional
    public PolicyResponse create(CreatePolicyRequest request) {
        if (policyRepository.existsByPolicyNo(request.policyNo())) {
            throw new DuplicatePolicyException(request.policyNo());
        }

        Customer customer = customerRepository.findByEmail(request.email())
                .orElseGet(() -> customerRepository.save(new Customer(request.customer(), request.email())));

        Policy policy = new Policy(
                request.policyNo(),
                customer,
                ProductType.valueOf(request.type()),
                request.basePremium(),
                PolicyStatus.valueOf(request.status())
        );

        return toResponse(policyRepository.save(policy));
    }

    @Transactional
    public void delete(String policyNo) {
        Policy policy = getPolicy(policyNo);
        policyRepository.delete(policy);
    }

    @Transactional(readOnly = true)
    public String customerNameFor(String policyNo) {
        return getPolicy(policyNo).getCustomer().getFullName();
    }

    private Policy getPolicy(String policyNo) {
        return policyRepository.findByPolicyNo(policyNo)
                .orElseThrow(() -> new PolicyNotFoundException(policyNo));
    }

    private PolicyResponse toResponse(Policy policy) {
        List<String> riderCodes = policy.getRiders().stream()
                .map(Rider::getCode)
                .sorted(Comparator.naturalOrder())
                .toList();

        return new PolicyResponse(
                policy.getPolicyNo(),
                policy.getCustomer().getFullName(),
                policy.getCustomer().getEmail(),
                policy.getProductType().name(),
                policy.getBasePremium(),
                policy.getStatus().name(),
                riderCodes
        );
    }

    private void validatePolicyStatus(PolicyStatus status) {
      if(status == null) {
            throw new InvalidRequestException("status must be Active, Lapsed, or Pending");
        }
    }

    private void validateProductType(ProductType type) {
        if(type == null) {
            throw new InvalidRequestException("type must be TERM, ULIP, or ENDOWMENT");
        }
    }
}
