package com.hdfclife.ledger.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "claims")
public class Claim {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "claim_no", nullable = false, unique = true)
    private String claimNo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id", nullable = false)
    private Policy policy;

    @Column(name = "amount", nullable = false)
    private int amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "urgency", nullable = false)
    private ClaimUrgency urgency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ClaimStatus status;

    protected Claim() {}

    public Claim(String claimNo, Policy policy, int amount, ClaimUrgency urgency, ClaimStatus status) {
        this.claimNo = claimNo;
        this.policy = policy;
        this.amount = amount;
        this.urgency = urgency;
        this.status = status;
    }

    public Long getId() { return id; }
    public String getClaimNo() { return claimNo; }
    public Policy getPolicy() { return policy; }
    public int getAmount() { return amount; }
    public ClaimUrgency getUrgency() { return urgency; }
    public ClaimStatus getStatus() { return status; }
}
