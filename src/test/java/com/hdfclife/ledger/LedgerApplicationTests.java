package com.hdfclife.ledger;

import com.hdfclife.ledger.repo.PolicyRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class LedgerApplicationTests {

    @Autowired
    private PolicyRepository policyRepository;

    @Test
    void seedLoadsExpectedPolicyCount() {
        assertEquals(6, policyRepository.count());
    }
}
