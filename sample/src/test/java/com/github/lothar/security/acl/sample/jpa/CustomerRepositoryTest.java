package com.github.lothar.security.acl.sample.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.github.lothar.security.acl.SimpleAclStrategy;
import com.github.lothar.security.acl.jpa.JpaSpecFeature;
import com.github.lothar.security.acl.sample.SampleApplication;
import com.github.lothar.security.acl.sample.domain.Customer;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SampleApplication.class)
@Transactional
public class CustomerRepositoryTest {

  @Resource
  private CustomerRepository repository;
  @Resource
  private SimpleAclStrategy customerStrategy;
  @Resource
  private JpaSpecFeature<Customer> jpaSpecFeature;
  private Logger logger = LoggerFactory.getLogger(getClass());

  @Before
  public void init() {
    repository.save(new Customer("Alice", "Smith"));
    repository.save(new Customer("Bob", "Smith"));
    repository.save(new Customer("John", "Doe"));
    logger.info("Installed feature : {}", customerStrategy.filterFor(jpaSpecFeature));
  }

  @Test
  public void should_customer_spec_be_registered_in_customer_strategy() {
    Specification<Customer> customerSpec = customerStrategy.filterFor(jpaSpecFeature);
    assertThat(customerSpec) //
        .as("Customer ACL JPA specification not registered") //
        .isNotNull();
  }

  @Test
  public void should_find_authorized_customers_only_when_strategy_applied() {
    assertThat(repository.count()).isEqualTo(2);
  }

  @Ignore("Once installed, that's it !")
  @Test
  public void should_find_all_customers_only_when_strategy_not_applied() {
    doWithoutCustomerSpec(new Runnable() {
      @Override
      public void run() {
        assertThat(repository.count()).isEqualTo(3);
      }
    });
  }

  private void doWithoutCustomerSpec(Runnable runnable) {
    Specification<Customer> customerSpec = customerStrategy.unregister(jpaSpecFeature);
    try {
      runnable.run();
    } finally {
      customerStrategy.register(jpaSpecFeature, customerSpec);
    }
  }

  @Ignore("Not implemented yet")
  @Test
  public void should_not_find_members_of_Doe_family_when_strategy_applied() {
    assertThat(repository.findByLastName("Doe")).isEmpty();
  }

}
