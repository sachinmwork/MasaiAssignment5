package com.hdfclife.ledger.config;

import com.hdfclife.ledger.domain.*;
import com.hdfclife.ledger.repo.CustomerRepository;
import com.hdfclife.ledger.repo.PolicyRepository;
import com.hdfclife.ledger.repo.RiderRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DataSeeder implements CommandLineRunner {
    private final CustomerRepository customerRepository;
    private final PolicyRepository policyRepository;
    private final RiderRepository riderRepository;
    private final Environment environment;

    public DataSeeder(CustomerRepository customerRepository,
                      PolicyRepository policyRepository,
                      RiderRepository riderRepository,
                      Environment environment) {
        this.customerRepository = customerRepository;
        this.policyRepository = policyRepository;
        this.riderRepository = riderRepository;
        this.environment = environment;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (policyRepository.count() > 0) {
            printStartup();
            return;
        }

        Customer anita = customerRepository.save(new Customer("Anita Sharma", "anita.sharma@hdfclife.example"));
        Customer rahul = customerRepository.save(new Customer("Rahul Mehta", "rahul.mehta@hdfclife.example"));
        Customer priya = customerRepository.save(new Customer("Priya Nair", "priya.nair@hdfclife.example"));
        Customer vikram = customerRepository.save(new Customer("Vikram Singh", "vikram.singh@hdfclife.example"));
        Customer sneha = customerRepository.save(new Customer("Sneha Patel", "sneha.patel@hdfclife.example"));

        Rider accident = riderRepository.findByCode("ACCIDENT_COVER").orElseThrow();
        Rider waiver = riderRepository.findByCode("WAIVER_OF_PREMIUM").orElseThrow();

        Policy p1001 = new Policy("HDFC-LIFE-1001", anita, ProductType.TERM, 18500, PolicyStatus.Active);
        p1001.getRiders().add(accident);
        p1001.getRiders().add(waiver);

        Policy p1002 = new Policy("HDFC-LIFE-1002", rahul, ProductType.ULIP, 42000, PolicyStatus.Active);
        Policy p1003 = new Policy("HDFC-LIFE-1003", priya, ProductType.ENDOWMENT, 27000, PolicyStatus.Lapsed);
        Policy p1004 = new Policy("HDFC-LIFE-1004", vikram, ProductType.TERM, 15200, PolicyStatus.Active);
        Policy p1005 = new Policy("HDFC-LIFE-1005", sneha, ProductType.ULIP, 36000, PolicyStatus.Active);
        Policy p1006 = new Policy("HDFC-LIFE-1006", anita, ProductType.ENDOWMENT, 22000, PolicyStatus.Pending);

        policyRepository.saveAll(List.of(p1001, p1002, p1003, p1004, p1005, p1006));
        printStartup();
    }

    private void printStartup() {
        String profile = environment.getActiveProfiles().length == 0
                ? "default"
                : environment.getActiveProfiles()[0];

        Policy p1004 = policyRepository.findByPolicyNo("HDFC-LIFE-1004").orElseThrow();
        List<Policy> premiumPolicies = policyRepository.findWithPremiumAtLeast(20000);
        Policy p1001 = policyRepository.findByPolicyNo("HDFC-LIFE-1001").orElseThrow();

        System.out.println("Active profile → " + profile);
        System.out.println("Policy count → " + policyRepository.count());
        System.out.println("Customer count → " + customerRepository.count());
        System.out.println("Lookup HDFC-LIFE-1004 customer → " + p1004.getCustomer().getFullName());
        System.out.println("Active count → " + policyRepository.findByStatusOrderByPolicyNoAsc(PolicyStatus.Active).size());
        System.out.println("TERM count → " + policyRepository.findByProductTypeOrderByPolicyNoAsc(ProductType.TERM).size());
        System.out.println("Anita Sharma policy count → " +
                policyRepository.findByCustomer_FullNameOrderByPolicyNoAsc("Anita Sharma").size());
        System.out.println("minPremium=20000 policy numbers → " +
                premiumPolicies.stream().map(Policy::getPolicyNo).collect(Collectors.joining(", ")));
        System.out.println("HDFC-LIFE-1001 rider codes → " +
                p1001.getRiders().stream().map(Rider::getCode).sorted().collect(Collectors.joining(", ")));
    }
}
