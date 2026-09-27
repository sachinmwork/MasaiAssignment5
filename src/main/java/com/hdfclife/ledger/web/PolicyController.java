package com.hdfclife.ledger.web;

import com.hdfclife.ledger.domain.PolicyStatus;
import com.hdfclife.ledger.domain.ProductType;
import com.hdfclife.ledger.dto.CreatePolicyRequest;
import com.hdfclife.ledger.dto.PolicyResponse;
import com.hdfclife.ledger.service.ClaimService;
import com.hdfclife.ledger.service.PolicyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/policies")
@Tag(name = "Policies", description = "Policy management APIs")
public class PolicyController {
    private final PolicyService policyService;
    private final ClaimService claimService;

    public PolicyController(PolicyService policyService, ClaimService claimService) {
        this.policyService = policyService;
        this.claimService = claimService;
    }

    @GetMapping
    @Operation(summary = "List policies", description = "Lists policies with optional status, type, or customer filters.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Policies returned")
    })
    public List<PolicyResponse> getPolicies(
            @RequestParam(required = false) PolicyStatus status,
            @RequestParam(required = false) ProductType type,
            @RequestParam(required = false) String customer) {

        if (status != null) return policyService.findByStatus(status);
        if (type != null) return policyService.findByType(type);
        if (customer != null) return policyService.findByCustomer(customer);
        return policyService.findAll();
    }

    @GetMapping("/search")
    @Operation(summary = "Search policies by minimum premium")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Matching policies returned"),
            @ApiResponse(responseCode = "400", description = "Invalid minimum premium")
    })
    public List<PolicyResponse> search(@RequestParam(required = false) Integer minPremium) {
        return policyService.searchByPremium(minPremium);
    }

    @GetMapping("/{policyNo}")
    @Operation(summary = "Get policy by policy number")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Policy returned"),
            @ApiResponse(responseCode = "404", description = "Policy not found")
    })
    public PolicyResponse getPolicy(@PathVariable String policyNo) {
        return policyService.findByPolicyNo(policyNo);
    }

    @PostMapping
    @Operation(summary = "Create policy")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Policy created"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "409", description = "Duplicate policy")
    })
    public ResponseEntity<PolicyResponse> create(@Valid @RequestBody CreatePolicyRequest request) {
        PolicyResponse response = policyService.create(request);
        return ResponseEntity.created(URI.create("/api/policies/" + response.policyNo())).body(response);
    }

    @DeleteMapping("/{policyNo}")
    @Operation(summary = "Delete policy")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Policy deleted"),
            @ApiResponse(responseCode = "404", description = "Policy not found")
    })
    public ResponseEntity<Void> delete(@PathVariable String policyNo) {
        policyService.delete(policyNo);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{policyNo}/claims")
    @Operation(summary = "List claims for a policy")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Claims returned"),
            @ApiResponse(responseCode = "404", description = "Policy not found")
    })
    public Object getClaims(@PathVariable String policyNo) {
        return claimService.findByPolicy(policyNo);
    }
}
