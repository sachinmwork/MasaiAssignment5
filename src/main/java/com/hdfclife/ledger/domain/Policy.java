package com.hdfclife.ledger.domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "policies")
public class Policy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "policy_no", nullable = false, unique = true)
    private String policyNo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_type", nullable = false)
    private ProductType productType;

    @Column(name = "base_premium", nullable = false)
    private int basePremium;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PolicyStatus status;

    @OneToMany(mappedBy = "policy", fetch = FetchType.LAZY)
    private List<Claim> claims = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "policy_riders",
        joinColumns = @JoinColumn(name = "policy_id"),
        inverseJoinColumns = @JoinColumn(name = "rider_id")
    )
    private List<Rider> riders = new ArrayList<>();

    protected Policy() {}

    public Policy(String policyNo, Customer customer, ProductType productType, int basePremium, PolicyStatus status) {
        this.policyNo = policyNo;
        this.customer = customer;
        this.productType = productType;
        this.basePremium = basePremium;
        this.status = status;
    }

    public Long getId() { return id; }
    public String getPolicyNo() { return policyNo; }
    public Customer getCustomer() { return customer; }
    public ProductType getProductType() { return productType; }
    public int getBasePremium() { return basePremium; }
    public PolicyStatus getStatus() { return status; }
    public List<Claim> getClaims() { return claims; }
    public List<Rider> getRiders() { return riders; }

    public void setCustomer(Customer customer) { this.customer = customer; }
    public void setProductType(ProductType productType) { this.productType = productType; }
    public void setBasePremium(int basePremium) { this.basePremium = basePremium; }
    public void setStatus(PolicyStatus status) { this.status = status; }
}
